package com.sanri.tools.modules.codepatch.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.HttpSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.Session;
import com.sanri.tools.modules.codepatch.service.dtos.*;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.dtos.param.AuthParam;
import com.sanri.tools.modules.core.dtos.param.GitParam;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.core.utils.NetUtil;
import com.sanri.tools.modules.core.utils.URLUtil;
import com.sanri.tools.modules.core.utils.ZipUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitService {

    @Autowired
    private FileManager fileManager;
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ConnectService connectService;

    private String baseDirName = "gitrepositorys";

    private static final String module = "git";

    @Autowired
    private WebSocketCompileService webSocketService;

    /**
     * @param group 表示连接管理中的连接名
     * @param url
     * @throws URISyntaxException
     * @throws GitAPIException
     */
    public void cloneRepository(String group,String url) throws URISyntaxException, GitAPIException, IOException {
        final URL repositoryURL = new URL(url);

        final File baseDir = fileManager.mkTmpDir(baseDirName);

        // 获取当前要创建的目录名,并检测是否存在
        final String repositoryName = FilenameUtils.getBaseName(URLUtil.pathLast(url));
        final File repositoryDir = new File(baseDir, group+"/"+repositoryName);
        boolean onlyGitHideDir = repositoryDir.exists() && repositoryDir.list().length == 1 && ".git".equals(repositoryDir.list()[0]);
        if (onlyGitHideDir){
            // 如果只有一个 .git 目录, 则为上次下载失败, 删除重新下载
            final File gitHideDir = new File(repositoryDir, ".git");
            FileUtils.deleteDirectory(gitHideDir);
        }
        if (repositoryDir.exists() && !ArrayUtils.isEmpty(repositoryDir.list())){
            log.error("仓库[{}]已经存在",repositoryDir.getAbsolutePath());
            throw new ToolException("仓库"+repositoryName+"已经存在");
        }
        repositoryDir.mkdirs();

        final CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setDirectory(repositoryDir);

        addAuth(group, cloneCommand);
        cloneCommand.call();
    }

    public List<String> groups(){
//        final File baseDir = fileManager.mkTmpDir(baseDirName);
//        return baseDir.list();
        return connectService.names(module);
    }

    public String[] repositorys(String group){
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File file = new File(baseDir, group);
        return file.list();
    }

    /**
     * 加载当前仓库所有的 pom 文件
     * @param group
     * @param repository
     * @return
     */
    public List<PomFile> loadAllPomFile(String group, String repository){
        final File repositoryDir = repositoryDir(group, repository);
        final Collection<File> files = FileUtils.listFiles(repositoryDir, new NameFileFilter("pom.xml"), TrueFileFilter.INSTANCE);
        List<PomFile> pomFiles = new ArrayList<>();
        for (File file : files) {
            final Path relativize = repositoryDir.toPath().relativize(file.toPath());
            final String moduleName = file.getParentFile().getName();
            pomFiles.add(new PomFile(repositoryDir,relativize.toString(),moduleName));
        }
        return pomFiles;
    }

    /**
     * 获取仓库最新的编译时间
     * @param group
     * @param repository
     * @return
     */
    public long newCompileTime(String group,String repository) throws IOException {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File infoFile = new File(baseDir,group + repository + "info");
        if (!infoFile.exists()){
            return -1;
        }
        final JSONObject jsonObject = JSON.parseObject(FileUtils.readFileToString(infoFile));
        final Iterator<String> iterator = jsonObject.keySet().iterator();
        long maxTime = -1;
        final String currentBranch = currentBranch(group, repository);
        while (iterator.hasNext()){
            final String key = iterator.next();
            if (key.startsWith("lastCompileSuccessTime_"+currentBranch+"_")){
                final Long aLong = jsonObject.getLong(key);
                if (aLong > maxTime ){
                    maxTime = aLong;
                }
            }
        }
        return maxTime;
    }

    /**
     * 获取模块信息
     * @param group
     * @param repository
     * @return
     */
    public List<Module> modules(String group,String repository) throws IOException {
        final List<PomFile> pomFiles = loadAllPomFile(group, repository);

        // 添加上次编译时间
        for (PomFile pomFile : pomFiles) {
            final String relativePath = pomFile.getRelativePath();
            final String pathMd5 = DigestUtils.md5DigestAsHex(relativePath.getBytes());
            final String currentBranch = currentBranch(group, repository);
            final Long property = (Long) configRepositoryProperty(group, repository, "lastCompileSuccessTime_"+currentBranch+"_" + pathMd5, null);
            if(property != null){
                pomFile.setLastCompileTime(new Date(property));
            }
        }

        // 构建模块
        if (CollectionUtils.isNotEmpty(pomFiles)) {
            Collections.sort(pomFiles);

            Function<String,Path> relativeToPath = relativePath -> {
                Path parent = Paths.get(relativePath).getParent();
                if (parent == null){
                    parent = Paths.get("/");
                }
                return parent;
            };

            final List<Path> pathList = pomFiles.stream().map(PomFile::getRelativePath).map(relativeToPath).collect(Collectors.toList());

            // 先映射成模块
            final Map<Path, Module> moduleMap = pomFiles.stream().map(Module::new).collect(Collectors.toMap(cur -> relativeToPath.apply(cur.getRelativePath()), Function.identity()));

            final Iterator<Path> iterator = pathList.iterator();
            while (iterator.hasNext()){
                final Path path = iterator.next();
                final Module findChildModule = moduleMap.get(path);
                for (Path curPath : pathList) {
                    final Module module = moduleMap.get(curPath);
                    final boolean isChildren = curPath.startsWith(path) && curPath != path && Math.abs(curPath.getNameCount() - path.getNameCount()) < 2;
                    if (isChildren){
                        findChildModule.getChildrens().add(module);

                        // 计算上次编译时间, 子模块如果旧于父模块编译时间,则继承父模块
                        final Date parentLastCompileTime = findChildModule.getLastCompileTime();
                        final Date childLastCompileTime = module.getLastCompileTime();
                        if (parentLastCompileTime != null && childLastCompileTime != null && childLastCompileTime.before(parentLastCompileTime)){
                            module.setLastCompileTime(parentLastCompileTime);
                        }else if (childLastCompileTime == null && parentLastCompileTime != null){
                            module.setLastCompileTime(parentLastCompileTime);
                        }

                    }
                }
            }

            // 查找顶部元素(同一方向路径最短)
            List<Module> tops = new ArrayList<>();
            Set<Path> routePaths = new HashSet<>();
            A: for (Path parent : pathList) {
                final Iterator<Path> routeIterator = routePaths.iterator();
                while (routeIterator.hasNext()){
                    Path path = routeIterator.next();
                    if (path.startsWith(parent)){
                        routeIterator.remove();
                        routePaths.add(parent);
                        continue A;
                    }
                    if (parent.startsWith(path)){
                        continue A;
                    }
                }
                routePaths.add(parent);
            }
            for (Path routePath : routePaths) {
                final Module topModule = moduleMap.get(routePath);
                tops.add(topModule);
            }

            return tops;
        }
        return new ArrayList<>();
    }


    /**
     * 编译文件
     * @param group
     * @param repository
     * @param pomRelativePath
     * @return 执行编译 mvn 退出码
     */
    public void compile(String ip,String websocketId,String group, String repository, String pomRelativePath) throws IOException, InterruptedException {
        final File repositoryDir = repositoryDir(group, repository);
        final File pomFile = repositoryDir.toPath().resolve(pomRelativePath).toFile();

        final GitParam gitParam = (GitParam) connectService.readConnParams(module, group);
        final String mavenHome = gitParam.getMavenHome();
        final String mavenConfigFilePath = gitParam.getMavenConfigFilePath();

        final String cmd = System.getProperty("os.name").contains("Linux") ? mavenHome+"/bin/mvn" : mavenHome+"/bin/mvn.cmd";
        final String [] cmdarray = new String[]{cmd,"-f",pomFile.getAbsolutePath(),"-s",mavenConfigFilePath,"-Dmaven.test.skip=true","clean","compile"};
        log.info("执行的命令为:{}", StringUtils.join(cmdarray," "));
        webSocketService.sendMessage(websocketId,StringUtils.join(cmdarray," "));
        final Process cleanCompile = Runtime.getRuntime().exec(cmdarray,null,new File(System.getProperty("user.home")));
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cleanCompile.getInputStream(), StandardCharsets.UTF_8));
        String line = "";
        while ((line = bufferedReader.readLine()) != null){
//            System.out.println(line);
            webSocketService.sendMessage(websocketId,line);
        }
        final int waitFor = cleanCompile.waitFor();
        if (waitFor == 0){
            // 记录上次编译成功时间
            final String pathMd5 = DigestUtils.md5DigestAsHex(pomRelativePath.getBytes());
            final String currentBranch = currentBranch(group, repository);
            configRepositoryProperty(group,repository,"lastCompileSuccessTime_"+currentBranch+"_"+ pathMd5,System.currentTimeMillis());
        }
        webSocketService.sendMessage(websocketId,waitFor+"");
        RuntimeUtil.destroy(cleanCompile);
    }

    /**
     * 添加或者获取仓库属性
     * @param group
     * @param repository
     * @param key
     * @param value
     * @throws IOException
     */
    public Object configRepositoryProperty(String group, String repository, String key, Object value) throws IOException {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File infoFile = new File(baseDir,group + repository + "info");
        JSONObject jsonObject = null;
        if (!infoFile.exists()){
            jsonObject = new JSONObject();
        }else{
            final String existJson = FileUtils.readFileToString(infoFile);
            jsonObject = JSON.parseObject(existJson);
            if (value == null){
                return jsonObject.get(key);
            }
        }

        jsonObject.put(key,value);
        FileUtils.writeStringToFile(infoFile,jsonObject.toJSONString());

        return jsonObject.get(key);
    }

    public void pull(String group, String repositoryName) throws IOException, GitAPIException, URISyntaxException {
        // 多人操作时, 拉取需要加锁
        lock(NetUtil.request().getRemoteAddr(),group,repositoryName);

        Git git = openGit(group, repositoryName);
        final PullCommand pullCommand = git.pull();
        addAuth(group,pullCommand);
        final PullResult pullResult = pullCommand.call();
        log.info("拉取数据结果",pullResult);
    }

    public List<Branchs.Branch> branchs(String group, String repositoryName) throws IOException, GitAPIException {
        Git git = openGit(group, repositoryName);
        final List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        List<Branchs.Branch> branches = new ArrayList<>();
        for (Ref ref : call) {
            final Branchs.Branch branch = new Branchs.Branch(ref.getName(),ref.getObjectId().name());
            branches.add(branch);
        }
        return branches;
    }

    public String currentBranch(String group,String repositoryName) throws IOException {
        Git git = openGit(group, repositoryName);

        final Repository repository = git.getRepository();
        return repository.getBranch();
    }

    public String switchBranch(String group, String repositoryName, String branchName) throws IOException, GitAPIException, URISyntaxException {
        // 多人操作时, 切换分支需要加锁
        lock(NetUtil.request().getRemoteAddr(),group,repositoryName);

        final Git git = openGit(group, repositoryName);
        //如果分支在本地已存在，直接checkout即可。
        if (this.branchNameExist(git, branchName)) {
            git.checkout().setCreateBranch(false).setName(branchName).call();
            final PullCommand pull = git.pull();
            addAuth(group,pull);
            pull.call();
        } else {
            //如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
            final Path fileName = Paths.get(branchName).getFileName();
            git.checkout().setCreateBranch(true).setName(fileName.toString()).setStartPoint(branchName).call();
        }

        return branchName;
    }

    public List<Commit> listCommits(String group,String repositoryName,int maxCount) throws IOException, GitAPIException {
        final Git git = openGit(group, repositoryName);
        List<Commit> commits = new ArrayList<>();

        final Iterable<RevCommit> revCommits = git.log().setMaxCount(maxCount).call();
        final Iterator<RevCommit> iterator = revCommits.iterator();
        while (iterator.hasNext()){
            final RevCommit revCommit = iterator.next();
            final String shortMessage = revCommit.getShortMessage();
            final String author = revCommit.getAuthorIdent().getName();
            final int commitTime = revCommit.getCommitTime();
            final ObjectId objectId = revCommit.getId();

            final Commit commit = new Commit(shortMessage, author, new String(objectId.name()), new Date(((long)commitTime) * 1000));
            commits.add(commit);
        }
        return commits;
    }

    Map<String,String> compilePath = new HashMap<>();
    {
        compilePath.put("src/main/java","classes");
        compilePath.put("src/main/resources","classes");
        compilePath.put("src/main/webapp","/");
    }

    /**
     * 选中多个 commitId 时, 创建补丁包
     * 从后往前, 找相临 commit 做文件差异, 然后后面的差异覆盖前面的差异
     * @param repository
     * @param group
     * @param commitIds
     * @return
     */
    public ChangeFiles createPatch(String group, String repositoryName, List<String> commitIds) throws IOException, GitAPIException {
        final Git git = openGit(group, repositoryName);

        final Repository repository = git.getRepository();

        Collections.reverse(commitIds);

        List<List<DiffEntry>> allDiffEntries = new ArrayList<>();
        try(TreeWalk treeWalk = new TreeWalk(repository);){
            for (String commitId : commitIds) {
                final RevCommit revCommit = repository.parseCommit(repository.resolve(commitId));
                try(RevWalk revWalk = new RevWalk(repository);){
                    revWalk.markStart(revCommit);
                    revWalk.next();
                    final RevCommit nextCommit = revWalk.next();
                    if (nextCommit != null){
                        treeWalk.reset(nextCommit.getTree());
                        treeWalk.setRecursive(true);
                        treeWalk.addTree(revCommit.getTree());
                        final List<DiffEntry> diffEntries = DiffEntry.scan(treeWalk);
                        allDiffEntries.add(diffEntries);
                    }else{
                        treeWalk.reset(revCommit.getTree());
                        treeWalk.setRecursive(true);
                        List<DiffEntry> diffEntries = new ArrayList<>();
                        while (treeWalk.next()){
                            final String pathString = treeWalk.getPathString();
                            final DiffEntryAdd diffEntryAdd = new DiffEntryAdd(pathString);
                            diffEntries.add(diffEntryAdd);
                        }
                        allDiffEntries.add(diffEntries);
                    }
                }
            }
        }

        // 得到最后的记录变更
        Map<String,DiffEntry> diffEntryMap = new LinkedHashMap();
        for (List<DiffEntry> diffEntries : allDiffEntries) {
            for (DiffEntry diffEntry : diffEntries) {
                final DiffEntry.ChangeType changeType = diffEntry.getChangeType();
                switch (changeType){
                    case DELETE:
                        final String oldPath = diffEntry.getOldPath();
                        diffEntryMap.put(oldPath,diffEntry);
                        break;
                    case MODIFY:
                    case ADD:
                    case COPY:
                    case RENAME:
                        final String newPath = diffEntry.getNewPath();
                        diffEntryMap.put(newPath,diffEntry);
                }
            }
        }

        final List<DiffEntry> diffEntries = new ArrayList<>(diffEntryMap.values());

        final File repositoryDir = repositoryDir(group, repositoryName);

        final ChangeFiles changeFiles = loadChangeFiles(repositoryDir,diffEntries);
        changeFiles.setCommitIds(commitIds);
        return changeFiles;
    }

    /**
     * 创建补丁包
     * @param group
     * @param repositoryName
     * @param commitBeforeId
     * @param commitAfterId
     * @return
     */
    public ChangeFiles createPatch(String group, String repositoryName, String commitBeforeId, String commitAfterId) throws IOException, GitAPIException {
        final Git git = openGit(group, repositoryName);
        final Repository repository = git.getRepository();

        try (TreeWalk treeWalk = new TreeWalk(repository);final RevWalk revWalk = new RevWalk(repository);) {
            final RevCommit revCommit = revWalk.parseCommit(repository.resolve(commitBeforeId));
            final RevCommit nextCommit = revWalk.parseCommit(repository.resolve(commitAfterId));

            treeWalk.addTree(revCommit.getTree());
            treeWalk.addTree(nextCommit.getTree());
            treeWalk.setRecursive(true);
            final List<DiffEntry> scan = DiffEntry.scan(treeWalk);
            final File repositoryDir = repositoryDir(group, repositoryName);
            final ChangeFiles changeFiles = loadChangeFiles(repositoryDir, scan);

            changeFiles.setCommitIds(Arrays.asList(commitBeforeId,commitAfterId));
            return changeFiles;
        }
    }

    private ChangeFiles loadChangeFiles(File repositoryDir,List<DiffEntry> diffEntries){
        List<FileInfo> modifyFileInfos = new ArrayList<>();
        List<FileInfo> deleteFileInfos = new ArrayList<>();
        for (DiffEntry diffEntry : diffEntries) {
            final DiffEntry.ChangeType changeType = diffEntry.getChangeType();
            if (diffEntry.getNewPath().contains("src/test")){
                log.info("测试文件夹跳过:{}",diffEntry);
                continue;
            }

            switch (changeType){
                case MODIFY:
                case ADD:
                case COPY:
                case RENAME:
                    File modifyFile = new File(repositoryDir, diffEntry.getNewPath());
                    if (diffEntry.getNewPath().contains("src/main/java")){
                        File compilePath = findCompilePath(modifyFile, repositoryDir);
                        final File modulePath = compilePath.getParentFile().getParentFile();
                        final Path relativePath = Paths.get("src/main/java").relativize(modulePath.toPath().relativize(modifyFile.toPath()));

                        final String extension = FilenameUtils.getExtension(modifyFile.getName());
                        if ("java".equals(extension)) {
                            // 在编译路径找到对应类, 需要包含内部类
                            final String baseName = FilenameUtils.getBaseName(modifyFile.getName());
                            final AndFileFilter andFileFilter = new AndFileFilter(new WildcardFileFilter(baseName + "*"), new SuffixFileFilter("class"));
                            final Collection<File> files = FileUtils.listFiles(compilePath, andFileFilter, TrueFileFilter.INSTANCE);
                            final Iterator<File> iterator = files.iterator();
                            while (iterator.hasNext()){
                                final File file = iterator.next();
                                if (!(baseName+".class").equals(file.getName()) && !file.getName().contains("$")){
                                    // 去掉找到的错误文件(名称同前缀的)
                                    iterator.remove();
                                }
                            }
                            final FileInfo fileInfo = new FileInfo(diffEntry, relativePath, files);
                            fileInfo.setModulePath(modulePath);
                            modifyFileInfos.add(fileInfo);
                        }else{
                            modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                        }
                    }else if (diffEntry.getNewPath().contains("src/main/resources")){
                        File compilePath = findCompilePath(modifyFile, repositoryDir);
                        final File modulePath = compilePath.getParentFile().getParentFile();
                        final Path relativePath = Paths.get("src/main/resources").relativize(modulePath.toPath().relativize(modifyFile.toPath()));
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }else if (diffEntry.getNewPath().contains("src/main/webapp")){
                        // 因为大多数项目前端端分离, webapp 不应该在用在分层的项目中
                        final Path relativePath = Paths.get("src/main/webapp").relativize(repositoryDir.toPath().relativize(modifyFile.toPath()));
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }else{
                        final Path relativePath = repositoryDir.toPath().relativize(modifyFile.toPath());
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }
                    break;
                case DELETE:
                    final String replace = diffEntry.getOldPath().replace("src/main/java", "target/classes").replace("src/main/resources", "").replace("src/main/webapp", "");
                    File deleteFile = new File(repositoryDir,replace);
                    final Path relativePath = repositoryDir.toPath().relativize(deleteFile.toPath());
                    deleteFileInfos.add(new FileInfo(diffEntry,relativePath));
                    break;

            }
        }

        return new ChangeFiles(modifyFileInfos,deleteFileInfos);
    }

    /**
     * 从变更记录中找到所有编译后的文件
     * @param group
     * @param repositoryName
     * @param diffEntries
     * @return
     * @throws IOException
     */
    public File findCompileFiles(String group, String repositoryName, ChangeFiles changeFiles) throws IOException {
        final List<FileInfo> modifyFileInfos = changeFiles.getModifyFileInfos();
        final List<FileInfo> deleteFileInfos = changeFiles.getDeleteFileInfos();

        // 创建压缩包
        final File patch = fileManager.mkTmpDir("gitpatch/" + System.currentTimeMillis());
        patch.mkdirs();

        // 写入总计信息
        final File allChange = new File(patch, "allchange.txt");
        List<String> allChangeText = new ArrayList<>();
        for (FileInfo fileInfo : modifyFileInfos) {
            allChangeText.add(fileInfo.getDiffEntry().toString());
        }
        for (FileInfo deleteFileInfo : deleteFileInfos) {
            allChangeText.add(deleteFileInfo.getDiffEntry().toString());
        }
        FileUtils.writeLines(allChange,allChangeText);

        final File modifyFileDir = new File(patch, "modify");

        if (CollectionUtils.isNotEmpty(modifyFileInfos)){
            modifyFileDir.mkdirs();

            for (FileInfo fileInfo : modifyFileInfos) {
                final DiffEntry diffEntry = fileInfo.getDiffEntry();
                final Path relativePath = fileInfo.getRelativePath();
                final File modulePath = fileInfo.getModulePath();
                final Collection<File> compileFiles = fileInfo.getCompileFiles();
                for (File compileFile : compileFiles) {
                    if (!compileFile.exists()){
                        final File parentFile = compileFile.getParentFile();
                        final String baseName = FilenameUtils.getBaseName(compileFile.getName());
                        final String extension = FilenameUtils.getExtension(compileFile.getName());
                        final File target = new File(parentFile, baseName + "丢失" + extension);
                        target.createNewFile();

                        compileFile = target;
                    }

                    Path resolve = relativePath.getParent() == null ? Paths.get(compileFile.getName()) : relativePath.getParent().resolve(compileFile.getName());

                    if (modulePath != null){
                        resolve = modulePath.toPath().getFileName().resolve(resolve);
                    }
                    final File file = modifyFileDir.toPath().resolve(resolve).toFile();
                    FileUtils.copyFile(compileFile,file);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(deleteFileInfos)){
            final File file = new File(patch, "delete.txt");

            List<String> deleteText = new ArrayList<>();
            for (FileInfo fileInfo : deleteFileInfos) {
                deleteText.add(fileInfo.getRelativePath().toString());
            }
            FileUtils.writeLines(file,deleteText);
        }

        final File zip = ZipUtil.zip(patch);

        FileUtils.deleteDirectory(patch);
        return zip;
    }


    /**
     * 根据文件,一级一级往上找, 直到找到 target 目录
     * @param file
     * @return
     */
    private File findCompilePath(File file,File stop){
        String compilePath = "target/classes";
        if ("pom.xml".equals(file)){
            return new File(file.getParent(),compilePath);
        }
        File parent = file;
        while (!parent.equals(stop) && !ArrayUtils.contains(parent.list(),"target")){
            parent = parent.getParentFile();
        }
        if (parent.equals(stop)){
            if (ArrayUtils.contains(parent.list(),"target")){
                return new File(parent,compilePath);
            }
            final Path resolve = stop.toPath().relativize(file.toPath());
            throw new IllegalStateException("是否还未编译, 没有找到 target 目录,在文件:"+resolve);
        }
        return new File(parent,compilePath);
    }

    private Git openGit(String group, String repositoryName) throws IOException {
        final File repositoryDir = repositoryDir(group, repositoryName);
        Git git = Git.open(repositoryDir);
        return git;
    }

    private File repositoryDir(String group, String repositoryName) {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File repositoryDir = new File(baseDir, group + "/" + repositoryName);
        return repositoryDir;
    }

    private void addAuth(String group, TransportCommand transportCommand) throws IOException, URISyntaxException {
        URIish urIish = null;
        if (transportCommand instanceof CloneCommand){
            CloneCommand cloneCommand = (CloneCommand) transportCommand;
            final Object uri = ReflectionUtils.getField(FieldUtils.getDeclaredField(CloneCommand.class, "uri",true), cloneCommand);
            urIish = new URIish(Objects.toString(uri));
        }else{
            final List<RemoteConfig> allRemoteConfigs = RemoteConfig.getAllRemoteConfigs(transportCommand.getRepository().getConfig());
            urIish = allRemoteConfigs.get(0).getURIs().get(0);
        }
        final GitParam gitParam = (GitParam) connectService.readConnParams(module, group);
        if ("git".equals(urIish.getScheme())){
            transportCommand.setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    SshTransport sshTransport = (SshTransport)transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                }
            });
        }else {
            final AuthParam authParam = gitParam.getAuthParam();
            if (authParam != null) {
                final UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(authParam.getUsername(), authParam.getPassword());
                transportCommand.setCredentialsProvider(usernamePasswordCredentialsProvider);
//                StoredConfig config = transportCommand.getRepository().getConfig();
//                if (config != null) {
//                    config.setBoolean("http", null, "sslVerify", false);
//                }
            }
        }
    }

    static {
        HttpTransport.setConnectionFactory(new InsecureHttpConnectionFactory());
    }

    public void lock(String remoteAddr,String group, String repository) throws IOException {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File lockFile = new File(baseDir,group + repository + "lock");
//        final String remoteAddr = NetUtil.remoteAddr();
        if (lockFile.exists()){
            final String ip = FileUtils.readFileToString(lockFile);
            if (StringUtils.isBlank(ip) || StringUtils.isBlank(remoteAddr) || remoteAddr.equals(ip)){
//                log.info("重入锁ip : {}",ip);
                // 可重入
                return ;
            }
            throw new IllegalStateException("当前仓库 "+group+" - "+repository+" 已经被" + ip + " 锁定");
        }
        // 锁定仓库
        FileUtils.writeStringToFile(lockFile,remoteAddr);
    }

    public void unLock(String group, String repository,boolean force) throws IOException {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File lockFile = new File(baseDir,group + repository + "lock");
        if (!lockFile.exists()){
            // 无需解锁
            return ;
        }
        final String remoteAddr = NetUtil.remoteAddr();
        final String ip = FileUtils.readFileToString(lockFile);
        if (!ip.equals(remoteAddr) && !force){
            throw new IllegalStateException("无法解锁,联系:"+ip);
        }
        final boolean delete = lockFile.delete();
        if (!delete){
            throw new ToolException("解锁失败, 联系管理者");
        }
    }

    static class InsecureHttpConnectionFactory implements HttpConnectionFactory {
        @Override
        public HttpConnection create(URL url ) throws IOException {
            return create( url, null );
        }

        @Override
        public HttpConnection create( URL url, Proxy proxy ) throws IOException {
            HttpConnection connection = new JDKHttpConnectionFactory().create( url, proxy );
            HttpSupport.disableSslVerify( connection );
            return connection;
        }
    }

    SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
		@Override
		protected void configure(OpenSshConfig.Host host, Session session) {
			/*
			 * 解除HostKey检查，也就意味着可以接受未知的远程主机的文件，这是不安全的，这种模式只是用于测试为目的的。
			 * 利用ssh-keyscan -t rsa hostname，收集主机数据。
			 */
			session.setConfig("StrictHostKeyChecking", "no");
		}
	};

    private boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branchName)) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module("docs").name("git").author("sanri").logo("git.jpg").desc("git代码管理").envs("default").build());
    }
}
