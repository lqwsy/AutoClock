package com.meteorshower.autoclock.view;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.meteorshower.autoclock.Job.TestClickJob;
import com.meteorshower.autoclock.JobThread.JobExecutor;
import com.meteorshower.autoclock.JobThread.JobFactory;
import com.meteorshower.autoclock.R;
import com.meteorshower.autoclock.bean.PostData;
import com.meteorshower.autoclock.presenter.JobPresenter;
import com.meteorshower.autoclock.presenter.JobPresenterImpl;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends BasicActivity implements JobView.AddJobView {

    @BindView(R.id.et_jobName)
    EditText jobName;
    @BindView(R.id.et_jobRemark)
    EditText jobRemark;
    @BindView(R.id.et_jobType)
    EditText jobType;

    private JobPresenter jobPresenter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_home;
    }

    @Override
    protected void initView() {
        jobPresenter = new JobPresenterImpl(this);
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.btn_start, R.id.btn_end, R.id.btn_add, R.id.btn_check, R.id.btn_look, R.id.btn_test, R.id.btn_test_click, R.id.btn_exc_cmd})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (!JobExecutor.getInstance().isRunning()) {
                    JobExecutor.getInstance().setRunning(true);
                    JobExecutor.getInstance().start();
                }
                if (!JobFactory.getInstance().isRunning()) {
                    JobFactory.getInstance().setRunning(true);
                    JobFactory.getInstance().start();
                }
                JobFactory.getInstance().setGetJob(true);
                break;
            case R.id.btn_end:
                JobFactory.getInstance().setGetJob(false);
                break;
            case R.id.btn_check:
                JobFactory.getInstance().setRunning(false);
                JobExecutor.getInstance().setRunning(false);
                break;
            case R.id.btn_look:
                startActivity(new Intent(HomeActivity.this, CheckJobActivity.class));
                break;
            case R.id.btn_add:
                try {
                    String sJobName = jobName.getText().toString();
                    String sJobRemark = jobRemark.getText().toString();
                    String sJobType = jobType.getText().toString();
                    int type = Integer.parseInt(sJobType);

                    PostData jobData = new PostData();
                    jobData.setJob_name(sJobName);
                    jobData.setExtra_info(sJobRemark);
                    jobData.setType(type);
                    jobPresenter.addNewJob(jobData);
                } catch (Exception e) {
                    Log.d("lqwtest", "add error = " + Log.getStackTraceString(e));
                }
                break;
            case R.id.btn_test:
                startActivity(new Intent(HomeActivity.this, CheckHeartActivity.class));
                break;
            case R.id.btn_test_click:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new TestClickJob(null).doJob();
                    }
                }).start();
                break;
            case R.id.btn_exc_cmd:
                startActivity(new Intent(HomeActivity.this, CommandExecuteActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void addSuccess() {
        jobName.setText("");
        jobType.setText("");
        jobRemark.setText("");
        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addFailure(String message) {
        Log.d("lqwtest", "addFailure message=" + message);
        Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
    }
}
