package com.Helpme.mylocation.persistence;

import com.Helpme.mylocation.model.User;
import com.Helpme.mylocation.model.User.Column;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserManager extends SQLiteOpenHelper implements UserManagerHelper {
	public static final String TAG = UserManager.class.getSimpleName();
    private SQLiteDatabase mDatabase;
    
	public UserManager(Context context) {
        super(context, UserManagerHelper.DATABASE_NAME, null, UserManagerHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	String CREATE_TABLE_USER = String.format("CREATE TABLE %s (%s TEXT, %s TEXT, %s TEXT ,%s TEXT,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)" ,
        User.TABLE,
        User.Column.CUSID,
        User.Column.USERNAME,
        User.Column.PASSWORD,
        User.Column.STATUSLOGIN,
        User.Column.STATUSSEND,
        User.Column.NAME,
        User.Column.PHONE,
        User.Column.ADDRESS,
        User.Column.PIN);	

    	db.execSQL(CREATE_TABLE_USER);
    	Log.i(TAG, CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	String DROP_USER = "DROP TABLE IF EXISTS " + UserManagerHelper.DATABASE_VERSION;
    	String DROP_CAR = "DROP TABLE IF EXISTS " + UserManagerHelper.DATABASE_VERSION;
        db.execSQL(DROP_USER);
        db.execSQL(DROP_CAR);
        Log.i(TAG, DROP_USER);
        Log.i(TAG, DROP_CAR);
        onCreate(mDatabase);
    }
  

    @Override
    public long registerUser(User user) {
    	mDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.Column.CUSID, user.getcusId());
        values.put(User.Column.USERNAME, user.getUsername());
        values.put(User.Column.PASSWORD, user.getPassword());
        values.put(User.Column.STATUSLOGIN, user.getStatus());
        values.put(User.Column.STATUSSEND, user.getStatussend());
        values.put(User.Column.NAME, user.getname());
        values.put(User.Column.PHONE, user.getphone());
        values.put(User.Column.ADDRESS, user.getaddress());
        
        String sql = "DELETE FROM User WHERE CUSID <>'"+user.getcusId()+"' ";
    	mDatabase.execSQL(sql); 
    	

        long result = mDatabase.insert(User.TABLE, null, values);
        mDatabase.close();
        
        
        
        mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.USERNAME,User.Column.PHONE,User.Column.ADDRESS};
    	Cursor c = mDatabase.query(User.TABLE,column, null, null, null, null, null);
    	User currentUser = new User();
    	if(c != null)
    	{	int count = c.getCount();
    		
    		for(int i = 0; i < count; i++)
    		{	c.moveToPosition(i);
    			
    			currentUser.setname(c.getString(0));
    			currentUser.setphone(c.getString(1));
    			currentUser.setaddress(c.getString(2));
    			
    			Log.d("testtest","name:"+c.getString(0));
    		}
    		c.close();
    		
    	}
    	
    	

        return result;
    }
    
    
    @Override
    public User checkUserLogin(User user) {
    	mDatabase = this.getReadableDatabase();
        Cursor cursor = mDatabase.query(User.TABLE,
        null,User.Column.USERNAME + " = ? AND " + User.Column.PASSWORD + " = ?",new String[]{user.getUsername(), user.getPassword()},null,null, null);

        User currentUser = new User();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentUser.setcusId(cursor.getString(0));
                currentUser.setUsername(cursor.getString(1));
                currentUser.setPassword(cursor.getString(2));
                currentUser.setStatus(cursor.getString(3));
                currentUser.setStatussend(cursor.getString(4));
                currentUser.setphone(cursor.getString(5));
                currentUser.setaddress(cursor.getString(6));
                mDatabase.close();
                return currentUser;
            }
        }

        return null;
    }
    
    public String getStat()
    {
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.STATUSLOGIN};
    	Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null, null);
    	if(c != null)
    	{
    		if(c.moveToFirst())
    		{
    			String stat = c.getString(0);
    			c.close();
    			return stat;
    		}
    	}
		return null;
    }
    
    public String getStatsend()
    {
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.STATUSSEND};
    	Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null, null);
    	if(c != null)
    	{
    		if(c.moveToFirst())
    		{
    			String statsend = c.getString(0);
    			c.close();
    			return statsend;
    		}
    	}
		return null;
    }
    
    public String getID()
    {
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.CUSID};
    	Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null, null);
    	if(c != null)
    	{
    		if(c.moveToFirst())
    		{
    			String ID = c.getString(0);
    			c.close();
    			return ID;
    		}
    	}
		return null;
    }
    
    public String getUsername()
    {
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.USERNAME};
    	Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null, null);
    	if(c != null)
    	{
    		if(c.moveToFirst())
    		{
    			String Username = c.getString(0);
    			c.close();
    			return Username;
    		}
    	}
		return null;
    }
    
    public String getPassword()
    {
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.PASSWORD};
    	Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null, null);
    	if(c != null)
    	{
    		if(c.moveToFirst())
    		{
    			String password = c.getString(0);
    			c.close();
    			return password;
    		}
    	}
		return null;
    }
    
    public void changepass(String oldpass , String newpass)
    {
    	String sql = "update User set password = '"+newpass+"' where password = '"+oldpass+"'";
		mDatabase.execSQL(sql);
    }
    
    public void setStat()
    {	String id = getID();
    	String sql = "update User set stat = '1' where cusid = "+id+"";
    	mDatabase.execSQL(sql);
    }
 
    public void resetStat()
    {	String id = getID();
    	String sql = "update User set stat = '0' where cusid = "+id+"";
    	mDatabase.execSQL(sql);
    }
    
    public void setStatsend(String set)
    {	String id = getID();
    	String statsend = set;
    	String sql = "update User set statsend = "+statsend+" where cusid = "+id+"";
    	mDatabase.execSQL(sql);
    }
    
    public User getuser()
    {	
    	mDatabase = this.getReadableDatabase();
    	String[] column ={User.Column.NAME,User.Column.PHONE,User.Column.ADDRESS};
    	Cursor c = mDatabase.query(User.TABLE,column, null, null, null, null, null);
    	User currentUser = new User();
    	if(c != null)
    	{	int count = c.getCount();
    		
    		for(int i = 0; i < count; i++)
    		{	c.moveToPosition(i);
    			
    			currentUser.setname(c.getString(0));
    			currentUser.setphone(c.getString(1));
    			currentUser.setaddress(c.getString(2));
    			

    		}
    		c.close();
    		return currentUser;
    	}
		return null;
		
    }
    
    @Override
    public int changePassword(User user) {
        mDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.Column.USERNAME, user.getUsername());
        values.put(User.Column.PASSWORD, user.getPassword());

        int row = mDatabase.update(User.TABLE,
                values,
                User.Column.CUSID + " = ?",
                new String[] {String.valueOf(user.getcusId())});

        mDatabase.close();
        return row;
    }
    
    public String getPin() {
		mDatabase = this.getReadableDatabase();
		String[] column = { User.Column.PIN };
		Cursor c = mDatabase.query(User.TABLE, column, null, null, null, null,
				null);
		if (c != null) {
			if (c.moveToFirst()) {
				String pin = c.getString(0);
				c.close();
				return pin;
			}
		}
		return null;
	}

	public void setPin(String pin) {
		String id = getID();
		String sql = "update User set pin = " + pin + " where cusid = " + id
				+ "";
		mDatabase.execSQL(sql);
	}
	
	public void resetPin() {
		String id = getID();
		String sql = "update User set pin = '0' where cusid = " + id + "";
		mDatabase.execSQL(sql);
	}

}
