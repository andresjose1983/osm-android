package com.osm.soft.sf.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.osm.soft.sf.BuildConfig;
import com.osm.soft.sf.OsmApplication;
import com.osm.soft.sf.model.Customer;
import com.osm.soft.sf.model.CustomerResponse;
import com.osm.soft.sf.rest.RestClient;
import com.osm.soft.sf.util.CustomerSqlHelper;
import com.osm.soft.sf.util.Functions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.client.Response;

/**
 * Created by Mendez Fernandez on 18/06/2016.º
 */
public class CustomerJobService extends Service {

    public static final long NOTIFY_INTERVAL = 11250;
    private CustomerSqlHelper customerSqlHelper;
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        customerSqlHelper = new CustomerSqlHelper(this);
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new Synchronize(), 0, NOTIFY_INTERVAL);
    }

    class Synchronize extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(() -> {
                if(Functions.getStatus(OsmApplication.getInstance()) &&
                        Functions.checkInternetConnection(OsmApplication.getInstance())){
                    if(!ProductsService.isRunning(CustomerJobService.this) &&
                            !CustomersService.isRunning(CustomerJobService.this) &&
                            !TaxTypesService.isRunning(CustomerJobService.this) &&
                            !LocalitiesService.isRunning(CustomerJobService.this)){
                        List<Customer> customers = customerSqlHelper.GET_PENDING.execute();
                        for (Customer customer : customers) {
                            Log.i(CustomerJobService.class.getCanonicalName(),
                                    customer.getCode() + " " + customer.isNew() + " " + customer.isSync());
                            if(customer.isNew())
                                create(customer);
                            else
                                update(customer);
                        }
                    }
                }
            });
        }
    }

    private void create(Customer customer){
        new Thread(() -> {
            Response execute = RestClient.CREATE_CUSTOMER.execute(new CustomerResponse(customer));
            if(execute != null) {
                if(BuildConfig.DEBUG)
                    Log.e(CustomerJobService.class.getCanonicalName(), "Code " + execute.getStatus());
                if(execute.getStatus() == 201) {
                    synced(customer);
                }
            }
        }).start();
    }

    private void update(Customer customer){
        new Thread(() -> {
            Response execute = RestClient.UPDATE.execute(new CustomerResponse(customer));
            if(execute != null) {
                if(BuildConfig.DEBUG)
                    Log.e(CustomerJobService.class.getCanonicalName(), "Code " + execute.getStatus());
                if(execute.getStatus() == 200) {
                    synced(customer);
                }
            }
        }).start();
    }

    private void synced(Customer customer){
        customer.setNew(false);
        customer.setSync(true);
        customerSqlHelper.UPDATE.execute(customer);
    }
}
