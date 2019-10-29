package minitest;

import com.sanri.app.jdbc.ExConnection;
import com.sanri.app.jdbc.PostgreSqlExConnection;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.postgresql.ds.PGSimpleDataSource;
import sanri.utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuickSearch {
    public static void main(String[] args) throws SQLException {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerName("10.101.72.78");
        pgSimpleDataSource.setPortNumber(5432);
        pgSimpleDataSource.setDatabaseName("hdsc_db");
        pgSimpleDataSource.setUser("postgres");
        pgSimpleDataSource.setPassword("postgres");

        ExConnection exConnection = ExConnection.newInstance(PostgreSqlExConnection.dbType, "test", pgSimpleDataSource);

        QueryRunner queryRunner = new QueryRunner(exConnection.getDataSource("hdsc_db"));
        BeanListHandler<MapBean> resultSetHandler = new BeanListHandler<MapBean>(MapBean.class);
        List<MapBean> query = queryRunner.query("SELECT uuid,material_file_id as url from id.id_material where delete_flag = 1 ", resultSetHandler);

        System.out.println("总数量 ："+query.size());
        List<MapBean> errors = new ArrayList<>();
        int count = 0;
        for (MapBean mapBean : query) {
            String url = mapBean.getUrl();
            try {
                System.out.println((count ++)+" 当前请求:"+url);
                String s = HttpUtil.get(url,null);
//                System.out.println(url +" 结果为:"+s);
                if(count %20 ==0){
                    System.out.println(errors);
                }
            } catch (Exception e){
                mapBean.setExceptionName(e.getClass().getName());
                errors.add(mapBean);
            }
        }
    }

   public  static class MapBean{
        private String uuid;
        private String url;
        private String exceptionName;

        public MapBean() {
        }

        public MapBean(String uuid, String url) {
            this.uuid = uuid;
            this.url = url;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

       @Override
       public String toString() {
            return exceptionName+"|"+uuid+":"+url+"\n";
       }

       public String getExceptionName() {
           return exceptionName;
       }

       public void setExceptionName(String exceptionName) {
           this.exceptionName = exceptionName;
       }
   }
}
