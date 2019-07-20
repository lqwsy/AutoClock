package com.meteorshower.autoclock.JobThread;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.meteorshower.autoclock.Job.AutoClickJob;
import com.meteorshower.autoclock.bean.HeatBeat;
import com.meteorshower.autoclock.bean.JobData;
import com.meteorshower.autoclock.http.ApiService;
import com.meteorshower.autoclock.http.RetrofitManager;
import com.meteorshower.autoclock.presenter.JobPresenter;
import com.meteorshower.autoclock.presenter.JobPresenterImpl;
import com.meteorshower.autoclock.util.StringUtils;
import com.meteorshower.autoclock.view.JobView;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobFactory extends Thread implements JobView.GetJobView {

    private JobPresenter jobPresenter;
    private int sleepTime = 5 * 60 * 1000;
    private boolean isGetJob = false;
    private boolean isRuning = true;

    private JobFactory() {
    }

    public static JobFactory getInstance() {
        return SingleTonHolder.sInstance;
    }

    private static class SingleTonHolder {
        private static JobFactory sInstance = new JobFactory();
    }


    @Override
    public void run() {
        Log.d("JobFactory", "JobFactory start -------------------------------- ");
        jobPresenter = new JobPresenterImpl(this);
        while (isRuning) {
            try {
                //每5分钟请求一次任务
                Thread.sleep(sleepTime);
                postToNet();
                Log.d("JobFactory", "isGetJob=" + isGetJob);
                if (!JobExecutor.getInstance().isDoingJob() && isGetJob) {
                    Log.d("JobFactory", "start get a job ");
                    jobPresenter.getCurrentJob(0, 1);
                }
            } catch (Exception e) {
                Log.d("JobFactory", "JobFactory run error: " + Log.getStackTraceString(e));
            }

        }
        Log.d("JobFactory", "JobFactory stop -------------------------------- ");
    }

    public boolean isRuning() {
        return isRuning;
    }

    public void setRuning(boolean runing) {
        isRuning = runing;
    }

    public boolean isGetJob() {
        return isGetJob;
    }

    public void setGetJob(boolean getJob) {
        isGetJob = getJob;
    }

    @Override
    public void getSuccess(List<JobData> jobList) {
        Log.d("JobFactory", "get job success " + new Gson().toJson(jobList));
        //获取到一次任务则睡眠5分钟
        sleepTime = 5 * 60 * 1000;
        try {
            if (jobList == null || jobList.size() <= 0) {
                return;
            }
            JobData jobData = jobList.get(0);
            AutoClickJob autoClickJob = new AutoClickJob(jobData);
            JobExecutor.getInstance().addJob(autoClickJob);
        } catch (Exception e) {
            Log.d("JobFactory", "JobFactory getSuccess error = " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void getFailure(String message) {
        Log.d("JobFactory", "get job getFailure message=" + message);
    }

    private void postToNet() {
        HeatBeat heatBeat = new HeatBeat();
        heatBeat.setHeart_time(StringUtils.getNow());
        heatBeat.setIs_doing_job(JobExecutor.getInstance().isDoingJob());
        heatBeat.setIs_getting_job(JobFactory.getInstance().isGetJob());

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(heatBeat));
        Call call = RetrofitManager.getInstance().getService(ApiService.class).postHeartBeat(body);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                //打印日志
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                //打印日志
            }
        });
    }
}
