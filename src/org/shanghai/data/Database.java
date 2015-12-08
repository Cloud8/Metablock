package org.shanghai.data;

import org.shanghai.util.FileUtil;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Alfred Anders
  @title Database Abstraction Layer
  @date 2013-11-16
*/
public class Database {

    private String server;
    private String database;
    private String dbuser;
    private String dbpass;

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement prep;

    public Database(String server, String db, String user, String pass) {
        this.server = server;
        this.database = db;
        this.dbuser = user;
        this.dbpass = pass;
    }

    public Database(String[] db) {
        this.server = db[0];
        this.database = db[1];
        this.dbuser = db[2];
        this.dbpass = db[3];
    }

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;        
    }

    public void create() {
        if (dataSource==null) {
            MysqlDataSource d = new MysqlDataSource();
            // allow column renaming (select a as b)
            d.setUseOldAliasMetadataBehavior(true); 
            d.setUser(dbuser);
            d.setPassword(dbpass);
            d.setServerName(server);
            d.setDatabaseName(database);
            try {
                connection = d.getConnection();
            } catch(SQLException e) { log(e); }
        } else {
            try {
                connection = dataSource.getConnection();
            } catch(SQLException e) { log(e); }
        }
    }

    public void dispose() {
        if (prep!=null) try {
            prep.close();
            prep = null;
        } catch(SQLException e) { log(e); }
        if (connection==null)
            return;
        try {
            connection.close();
        } catch(SQLException e) { log(e); }
        connection=null;
    }

    private void log(String msg) {
        Logger.getLogger(Database.class.getName()).info(msg);
    }

    private void warning(String msg) {
        Logger.getLogger(Database.class.getName()).warning(msg);
    }

    private void log(Exception e) {
        log(e.toString());
		e.printStackTrace();
		//System.exit(1);
    }

    public ResultSet getSingleRow(String query) {
        ResultSet rs = null;
        try { 
           PreparedStatement stmt = connection.prepareStatement(query);
           stmt.setMaxRows(1);
           rs = stmt.executeQuery(query); 
        } catch (SQLException ex) {
            warning("Error query: " + query + " : " + ex.toString());
        } finally {
            return rs;
        }
    }

    public ResultSet getResult(String query) {
        ResultSet rs = null;
        try { 
            Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //  ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            //PreparedStatement stmt = connection.prepareStatement(query);
            //rs = stmt.executeQuery(query); 
        } catch (SQLException ex) {
            warning("Error result: " + query + " : " + ex.toString());
        } finally {
            return rs;
        }
    }

    public int update(String query) {
      int res=0;
      try {
         //PreparedStatement pstmt = con.prepareStatement(query);
	     //res = pstmt.executeUpdate(query);
         Statement statement = connection.createStatement();
         statement.setEscapeProcessing(true);
	     res = statement.executeUpdate(query);
         //log("res " + res + ": " + query);
       } catch(SQLException ex) {
         warning("Error connecting: " + ex.toString() );
         log("[" + query + "]");
       }
      return res;
    }

    public int update(String query, String[] params) {
      int res=0;
      try {
         if (prep==null) {
             prep = connection.prepareStatement(query);
         }
         int ii=1; for (String param : params) {
             prep.setString(ii++, param); 
         }
	     res = prep.executeUpdate();
       } catch(MySQLIntegrityConstraintViolationException ex) {
         // ignore 
       } catch(SQLException ex) {
         warning("Error connecting: " + ex.toString() );
         log("[" + query + "]");
       }
      return res;
    }

    public String getSingleText(String query)
    {
        String result = null;
        try { 
           PreparedStatement stmt = connection.prepareStatement(query);
           stmt.setMaxRows(1);
           ResultSet rs = stmt.executeQuery(query); 
           while (rs.next()) {
                result = rs.getString(1);
           }
        } catch (SQLException ex) {
            warning("Error text: " + query + " : " + ex.toString());
        } finally {
            return result;
        }
    }

    public List<String> getColumn(String query, int limit) {
        List<String> results = new ArrayList<String>();
        int column = 1;
        int count=0;
        try {
            Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(query);
            if (rs.isBeforeFirst()) {
                while(rs.next()) {
				    results.add(rs.getString(column));
                    count++;
                    if (count>limit)
                        break;
                }
            }
       } catch(SQLException ex) {
           log(query);
           log(ex);
       }
       return results;
    }

    private int countOccurrences(String haystack, char needle)
    {
        int count = 0;
        for (int i=0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                 count++;
            }
        }
        return count;
    }

    public int getSingleInt(String query)
    {
        int result = 0;
        try {
            //log(query);
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setMaxRows(1);
            ResultSet rs = stmt.executeQuery(query); 
            while (rs.next()) {
                //log(rs.toString());
                result = rs.getInt(1);
            }
         } catch (SQLException ex) {
            warning("Error first int : " + query + " : " + ex.toString());
         }
         return result;
    }

  public void executeSql(String command) {
      //getDb().update("set names utf8");
      int i = 0;
      int ch = command.indexOf(';');
      String update;
      while ( ch > -1 ) {
          update = command.substring(i, ch).trim();
          i = ch+1;
          ch = command.indexOf(';',i);
          this.update(update);
      }
  }

  public void executeFile(String filename) {
      String command = FileUtil.read(filename);
      executeSql(command);
  }
}
