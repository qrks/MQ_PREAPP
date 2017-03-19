package preapp;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyUtil {
	public static Connection getConn(String dbstr) {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			Connection conn =
				DriverManager.getConnection(dbstr);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void closeSrc(Object o) {
		if(o == null)
			return;
		if(o instanceof ResultSet) {
			try {
				((ResultSet)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else
		if(o instanceof Statement) {
			try {
				((Statement)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else 
		if(o instanceof PreparedStatement) {
			try {
				((PreparedStatement)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else 
		if(o instanceof Connection) {
			try {
				((Connection)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else 
		if(o instanceof ServerSocket) {
			try {
				((ServerSocket)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else 
		if(o instanceof Socket) {
			try {
				((Socket)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}else 
		if(o instanceof OutputStream) {
			try {
				((OutputStream)o).close();
			} catch (Exception e) {
			} finally {
				if(o != null)
					o = null;
			}
		}
		
		if(o != null)
			o = null;
		
	}
	
	public static String simpleQuery(String sql,Connection conn) {
		Statement s1 = null;
		ResultSet rs1 = null;
		String ret = "";
		try {
			s1 = conn.createStatement();
			rs1 = s1.executeQuery(sql);
			for(;rs1.next();) {
				ret = rs1.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeSrc(rs1);
			closeSrc(s1);
		}
		
		return ret;
	}
	
	public static String getExcpMsg(Exception e) {
		String nextline = System.getProperty("line.separator");
		StackTraceElement[] ss = e.getStackTrace();
		String errinfo = "";
		for (int i = 0; i < ss.length; i++) {
			String tmp = ss[i].getClassName() + "." + ss[i].getMethodName() + "@" + ss[i].getLineNumber();
			errinfo += tmp + nextline;
		}
		return e.getMessage() + nextline + errinfo;
	}
	
	public static void printArray(String ss[][]) {
		for(int i=0; i<ss.length; i++) {
			for(int j=0; j<ss[i].length; j++) {
				System.out.println(ss[i][j]);
			}
		}
	}
	
	public static String byte2hex(byte[] bytes)
	{
		int len=bytes.length;
		String ret="";
		for(int i=0;i<len;i++)
		{
			int k=bytes[i];
			if(k<0)
			{
				k+=256;
			}
			String s=Integer.toHexString(k).toUpperCase();
			if(s.length()==1){
				s="0"+s;
			}
			ret+=s;
		}
//		System.out.println(ret);
		return ret;
	}
	
	public static byte[] hex2byte(String s)
	{
		s=s.toUpperCase();
		int len=s.length()/2;
		byte[] ba=new byte[len];
		for(int i=0;i<len;i++)
		{
			String s1=s.substring(i*2,(i+1)*2);
			int bi=Integer.valueOf(s1,16);
			ba[i]=(byte) bi;
		}
		return ba;
	}
	
}
