package learntest;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GitMain {
//    @Test
//    public void testCommits() throws IOException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        builder.findGitDir(new File(System.getProperty("user.dir")));
//        Repository repository = builder.build();
//        String branch = repository.getBranch();
//        System.out.println(branch);
//    }

    @Test
    public void testCommits() throws IOException, GitAPIException {
        Git git = Git.open(new File(System.getProperty("user.dir")));
        Iterable<RevCommit> revCommits = git.log().call();
        Iterator<RevCommit> iterator = revCommits.iterator();
        while (iterator.hasNext()){
            RevCommit revCommit = iterator.next();
            String commitId = revCommit.getName();
            Date commitTime = revCommit.getAuthorIdent().getWhen();
            String name = revCommit.getAuthorIdent().getName();
            String fullMessage = revCommit.getFullMessage();
            System.out.println(commitId+","+name+" 在 "+ DateFormatUtils.ISO_DATETIME_FORMAT.format(commitTime)+" 提交信息为 :"+fullMessage);
        }
    }

    /**
     * https://blog.csdn.net/qq_42331185/article/details/96111550
     * @throws IOException
     * @throws GitAPIException
     */
    @Test
    public void getVersionFiles() throws IOException, GitAPIException {
        String commitId = "7fe946f8ceba2f0c36ec9be0b31b06536aa9fb73";
        Git git = Git.open(new File(System.getProperty("user.dir")));
        Repository repository = git.getRepository();
        ObjectId resolve = repository.resolve(commitId);

        Iterable<RevCommit> allCommitsLater = git.log().add(resolve).call();
        Iterator<RevCommit> iterator = allCommitsLater.iterator();
        RevCommit commit = iterator.next();

        // 给存储库创建一个树的遍历器
        TreeWalk tw = new TreeWalk(repository);
        // 将当前commit的树放入树的遍历器中
        tw.addTree(commit.getTree());

        commit = iterator.next();
        if (commit != null) {
            tw.addTree(commit.getTree());
        } else {
//            return null;
        }

        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        //获取到详细记录结果集    在这里就能获取到一个版本号对应的所有提交记录（详情！！！）
        List<DiffEntry> list = rd.compute();

        System.out.println(list);
    }
}
