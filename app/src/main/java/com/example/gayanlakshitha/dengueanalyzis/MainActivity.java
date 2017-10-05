package com.example.gayanlakshitha.dengueanalyzis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Location location;
    private Button btnGPS;
    private Button btnFind;
    private Spinner spin_loc;
    private ListView lst_MyLoc;
    private ListView lst_Recent;
    private Button btnSync;
    private Button btnAdd;
    private Button btnRemove;

    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        btnGPS = (Button)findViewById(R.id.btn_GPS);
        btnFind = (Button)findViewById(R.id.btn_find);
        lst_MyLoc = (ListView)findViewById(R.id.lst_mylocation);
        spin_loc = (Spinner)findViewById(R.id.spinner);
        btnSync = (Button)findViewById(R.id.btn_sync);
        btnAdd = (Button)findViewById(R.id.btn_addtoMy);
        btnRemove = (Button)findViewById(R.id.btn_removefromMy);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference location_ref = database.getReferenceFromUrl("https://dengueanalyzis.firebaseio.com/Locations/");

        final SQLiteDatabase location_db = openOrCreateDatabase("locationdb.db",MODE_PRIVATE,null);
        location_db.execSQL("CREATE TABLE IF NOT EXISTS tbl_location(id INTEGER PRIMARY KEY AUTOINCREMENT,location text,latitude double,longitude double,risk boolean,patients int, deaths int)");
        location_db.execSQL("CREATE TABLE IF NOT EXISTS tbl_mylocation(location text)");

        ArrayList<String> loc_list = new ArrayList<>();
        final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),R.layout.spiner,loc_list);
        SyncData(location_db,location_ref,adapter);

        final Cursor cursor = location_db.rawQuery("SELECT * FROM tbl_location",null);

        while(cursor.moveToNext())
        {
            loc_list.add(cursor.getString(1));
        }
        //Create ArrayAdapter and set it to Spinner

        spin_loc.setAdapter(adapter);

        fillMyLocation(location_db);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spin_loc.getCount()==0)
                    Toast.makeText(getApplicationContext(),"Location Database is Empty.\nPlease Sync Data",Toast.LENGTH_SHORT).show();
                else
                {
                    GPSTracker tracker = new GPSTracker(getApplicationContext());
                    location = tracker.getLocation();
                    if(location!=null)
                    {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        String location = getShortestPosition(location_db,lon,lat);
                        Toast.makeText(getApplicationContext(),"Your Nearest Location : " + location,Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        spin_loc.setSelection(adapter.getPosition(location));
                    }
                }

            }
        });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Please Switch on Internet Service. \nSync Process May Take a Moment",Toast.LENGTH_SHORT).show();

                SyncData(location_db,location_ref,adapter);
                //Firebase and SQLite Sync

                Toast.makeText(getBaseContext(),"Sync Process Complete",Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    String location = spin_loc.getSelectedItem().toString();
                    if(location.isEmpty())
                        Toast.makeText(getApplicationContext(),"Select a Location",Toast.LENGTH_SHORT).show();
                    else
                    {
                        Cursor my_cursor = location_db.rawQuery("SELECT * FROM tbl_mylocation",null);
                        boolean flag = false;

                           while(my_cursor.moveToNext())
                            {
                               if(location.equals(my_cursor.getString(0)))
                               {
                                   flag=true;
                                   break;
                               }

                            }

                            if(!flag)
                            {
                                location_db.execSQL("INSERT INTO tbl_mylocation (location) VALUES ('"+location+"')");
                                fillMyLocation(location_db);
                            }


                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Select a Location",Toast.LENGTH_SHORT).show();
                }


            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    String location = spin_loc.getSelectedItem().toString();
                    if(location.isEmpty())
                        Toast.makeText(getApplicationContext(),"Select a Location",Toast.LENGTH_SHORT).show();
                    else
                    {
                        Cursor my_cursor = location_db.rawQuery("SELECT * FROM tbl_mylocation",null);
                        boolean flag = false;

                        while(my_cursor.moveToNext())
                        {
                            if(location.equals(my_cursor.getString(0)))
                            {
                                flag=true;
                                break;
                            }

                        }

                        if(flag)
                        {
                            location_db.execSQL("DELETE FROM tbl_mylocation WHERE location='"+location+"'");
                            fillMyLocation(location_db);
                        }
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Your Location List is Empty",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    String location = spin_loc.getSelectedItem().toString();
                    if(location.isEmpty())
                        Toast.makeText(getApplicationContext(),"Please Select a Location or Sync Data",Toast.LENGTH_SHORT).show();
                    else
                    {
                        Intent intent = new Intent(MainActivity.this,Information.class);
                        Bundle b = new Bundle();
                        b.putString("location",location);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Please Select a Location or Sync Data",Toast.LENGTH_SHORT).show();
                }


            }
        });

        lst_MyLoc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean flag = false;
                String location = lst_MyLoc.getItemAtPosition(position).toString();
                Cursor my_cursor = location_db.rawQuery("SELECT location FROM tbl_mylocation",null);

                while(my_cursor.moveToNext())
                {
                    if(location.equals(my_cursor.getString(0)))
                    {
                        flag = true;
                        break;
                    }
                }

                if(flag)
                {
                    Intent intent = new Intent(MainActivity.this,Information.class);
                    Bundle b = new Bundle();
                    b.putString("location",location);
                    intent.putExtras(b);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(),"Location Not Exists",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void fillMyLocation(SQLiteDatabase db)
    {
        Cursor my_cursor = db.rawQuery("SELECT * FROM tbl_mylocation",null);
        List<String> my_loc_list = new ArrayList<>();
        while(my_cursor.moveToNext())
            my_loc_list.add(my_cursor.getString(0));
        ArrayAdapter my_loc_Adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,my_loc_list);
        lst_MyLoc.setAdapter(my_loc_Adapter);
    }

    private void SyncData(final SQLiteDatabase location_db, DatabaseReference location_ref,final ArrayAdapter adapter)
    {
        location_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                location_db.execSQL("DELETE FROM tbl_location");

                for(DataSnapshot child : dataSnapshot.getChildren())
                {
                    LocationInfo info = child.getValue(LocationInfo.class);

                    try
                    {
                        location_db.execSQL("INSERT INTO tbl_location (location,latitude,longitude,risk,patients,deaths) VALUES" +
                                "('"+info.getLocation()+"','"+info.getLatitude()+"','"+info.getLongitude()+"'," +
                                "'"+info.isRisk()+"','"+info.getPatients()+"','"+info.getDeaths()+"')");

                    }
                    catch (SQLException e)
                    {

                    }

                }

                setSpinner(location_db,adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void setSpinner(SQLiteDatabase location_db,ArrayAdapter arradp)
    {
        Cursor cursor = location_db.rawQuery("SELECT location FROM tbl_location",null);
        ArrayList<String> loc_list = new ArrayList<>();
        while(cursor.moveToNext())
            loc_list.add(cursor.getString(0));
        //Create ArrayAdapter and set it to Spinner
        final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),R.layout.spiner,loc_list);
        spin_loc.setAdapter(adapter);
        arradp.notifyDataSetChanged();
    }

    private String getShortestPosition(SQLiteDatabase db,double mylongitude, double mylatitude)
    {
        double longitude=0;
        double latitude=0;
        double distance=0;
        double min_length=0;
        int count=0,position=0;

        Cursor cursor = db.rawQuery("SELECT latitude,longitude,location FROM tbl_location",null);
        double distancearr[] = new double[cursor.getCount()];
        String locationarr[] = new String[cursor.getCount()];

        //cursor.moveToFirst();
        while(cursor.moveToNext())
        {
            latitude = cursor.getDouble(0);
            longitude = cursor.getDouble(1);
            distance = getDistance(latitude,mylatitude,longitude,mylongitude);

            distancearr[count] = distance;
            locationarr[count] = cursor.getString(2);
            count++;
        }

        min_length = distancearr[0];
        //count = cursor.getCount();
        for(int i=0;i<count;i++)
        {
            if(min_length>=distancearr[i])
            {
                min_length=distancearr[i];
                position=i;
            }
        }

        return locationarr[position];
    }

    private double getDistance(double lat1, double lat2, double lon1, double lon2)
    {
        return Math.sqrt(Math.pow(lat1-lat2,2)+Math.pow(lon1-lon2,2));
    }
}

