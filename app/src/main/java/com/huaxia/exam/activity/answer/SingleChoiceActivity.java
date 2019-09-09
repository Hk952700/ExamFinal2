package com.huaxia.exam.activity.answer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxia.exam.R;
import com.huaxia.exam.adapter.SingleChoiceRecyclerViewAdapter;
import com.huaxia.exam.base.BaseActivity;
import com.huaxia.exam.bean.AnswerResultDataBean;
import com.huaxia.exam.bean.SingleChoiceItemBean;
import com.huaxia.exam.bean.UploadGradeDataBean;
import com.huaxia.exam.utils.SharedPreUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.huaxia.exam.utils.AnswerConstants.ANSWER_QUESTION_SUM;

/**
 * 2019年4月12日 10:22:14
 * jiao  hao kang
 * 单选题 activity
 */
public class SingleChoiceActivity extends BaseActivity implements View.OnClickListener {
    private RecyclerView mOptionRecyclerview;
    private TextView mQuestion;
    //private TextView mCountDownText;
    private Button mConfirmButton;
    private SingleChoiceItemBean mUserAnswer;

    private AnswerResultDataBean data;

    private long date;
    private long submitTime;
    private RelativeLayout mWebsocket_status;
    private AlertDialog alertDialog;
    private LayoutInflater factory;
    private View view;
    private String[] split;
    private TextView mTvUsername;
    private TextView mTvNumberplate;
    private TextView mTvAnswerNum;
    private ImageView mIvCountTens;
    private ImageView mIvCountOnes;
    private boolean mIsLastQuestion = false;
    private AnimatorSet animatorSet;
    private ConstraintLayout amswer_results_bg0;
    private ConstraintLayout amswer_results_bg1;
    private ConstraintLayout amswer_results_bg2;


    @Override
    public int setContentView() {
        return R.layout.activity_single_choice;
    }

    @Override
    public Context setContext() {
        return SingleChoiceActivity.this;
    }


    @Override
    public void init() {
        //右上角WebSocket状态方框
        mWebsocket_status = (RelativeLayout) findViewById(R.id.single_choice_websocket_status);

        //mCountDownText = (TextView) findViewById(R.id.single_choice_count_down);
        mQuestion = (TextView) findViewById(R.id.single_choice_question);
        mOptionRecyclerview = (RecyclerView) findViewById(R.id.single_choice_option_recyclerview);

        //确认图片
        mConfirmButton = (Button) findViewById(R.id.single_choice_confirm_button);

        mTvUsername = (TextView) findViewById(R.id.single_choice_username);
        mTvNumberplate = (TextView) findViewById(R.id.single_choice_numberplate);
        mTvAnswerNum = (TextView) findViewById(R.id.single_choice_answer_num);

        mIvCountTens = (ImageView) findViewById(R.id.single_choice_count_down_tens);
        mIvCountOnes = (ImageView) findViewById(R.id.single_choice_count_down_ones);

        initDataAndRecycler();


    }


    private void initDataAndRecycler() {
        Intent intent = getIntent();
        data = (AnswerResultDataBean) intent.getParcelableExtra("answer");

        Log.i("jtest", "initDataAndRecycler: " + data);
        if (data != null) {


            mTvUsername.setText(SharedPreUtils.getString(this, "user_name"));
            mTvAnswerNum.setText(data.getTp_senum() + "/" + ANSWER_QUESTION_SUM);
            mTvNumberplate.setText(SharedPreUtils.getString(this, "user_numberplate") + "号");


            if (data.getTp_subject().length() <= 13) {
                mQuestion.setTextSize(this.getResources().getDimension(R.dimen.sp_36));
            } else if (data.getTp_subject().length() <= 18 && data.getTp_subject().length() > 13) {
                mQuestion.setTextSize(this.getResources().getDimension(R.dimen.sp_28));
            } else {
                mQuestion.setTextSize(this.getResources().getDimension(R.dimen.sp_18));
            }


            mQuestion.setText(data.getTp_subject().trim());

            ArrayList<SingleChoiceItemBean> singleChoiceItemBeans = new ArrayList<>();
            split = data.getTp_options().split("/");
            for (int i = 0; i < split.length; i++) {
                char[] chars = split[i].toCharArray();
                StringBuffer stringBuffer = new StringBuffer();
                for (int j = 0; j < chars.length; j++) {
                    if (j > 0) {
                        stringBuffer.append(chars[j]);
                    }
                }

                singleChoiceItemBeans.add(new SingleChoiceItemBean(String.valueOf(chars[0]), stringBuffer.toString(), false, false));
            }
            singleChoice(singleChoiceItemBeans);
        }

        startCountDown(mIvCountTens, mIvCountOnes, 20);
        mConfirmButton.setOnClickListener(this);
    }


    @Override
    public void onCountDownFinish(long date, long submitTime) {
        super.onCountDownFinish(date, submitTime);
        this.date = date;
        this.submitTime = submitTime;
        if (data != null) {
            if (data.getTp_senum() == ANSWER_QUESTION_SUM) {
                mIsLastQuestion = true;
            } else {
                mIsLastQuestion = false;
            }
        }
        UploadGradeDataBean uploadGradeDataBean = new UploadGradeDataBean();


        if (mUserAnswer != null) {


            if (data.getTp_answer().equals(mUserAnswer.getOption())) {
                uploadGradeDataBean.setTrRight("0");//对错

                Log.i("jtest", "onCountDownFinish:分数=" + data.getTp_score());
                //分数
                uploadGradeDataBean.setTrMark(data.getTp_score() + "");
                String ss = new String();
                if (data != null) {

                    String tp_answer = data.getTp_answer();


                    for (int i = 0; i < split.length; i++) {
                        char[] chars = split[i].toCharArray();
                        if (String.valueOf(chars[0]).trim().equals(tp_answer.trim())) {
                            //ss += split[i].substring(1, split[i].length()).trim();
                            ss = String.valueOf(chars[0]) + ":" + split[i].substring(1, split[i].length()).trim();
                        }
                    }
                }
                showDiaLog(0, ss.toString(), mUserAnswer.getOption() + ":" + mUserAnswer.getContext());
            } else {

                String ss = new String();
                //StringBuffer stringBuffer = new StringBuffer();
                if (data != null) {
                    String tp_answer = data.getTp_answer();


                    for (int i = 0; i < split.length; i++) {
                        char[] chars = split[i].toCharArray();
                        if (String.valueOf(chars[0]).trim().equals(tp_answer.trim())) {
                            ss = String.valueOf(chars[0]) + ":" + split[i].substring(1, split[i].length()).trim();
                        }
                    }
                }

                //对错
                uploadGradeDataBean.setTrRight("1");
                //分数
                uploadGradeDataBean.setTrMark("0");
                showDiaLog(1, ss.toString(), mUserAnswer.getOption() + ":" + mUserAnswer.getContext());
            }
            //学生答案
            uploadGradeDataBean.setTrAnswer(mUserAnswer.getOption());


        } else {
            StringBuffer stringBuffer = new StringBuffer();
            if (data != null) {
                String tp_answer = data.getTp_answer();


                for (int i = 0; i < split.length; i++) {
                    char[] chars = split[i].toCharArray();
                    if (String.valueOf(chars[0]).trim().equals(tp_answer.trim())) {
                        stringBuffer.append(split[i].substring(1, split[i].length()).trim());
                    }
                }
            }

            showDiaLog(1, stringBuffer.toString(), "未作答");

            //学生答案
            uploadGradeDataBean.setTrAnswer("未作答");
            //分数
            uploadGradeDataBean.setTrMark("0");
            //对错
            uploadGradeDataBean.setTrRight("1");
        }
        if (data != null) {
            //班级
            uploadGradeDataBean.setTrClass(data.getTp_class() + "");
            //耗时
            uploadGradeDataBean.setTrTime(date + "");
            uploadGradeDataBean.setTrQuestion(data.getTp_subject());
            //题号
            uploadGradeDataBean.setTrPapernum(data.getTp_senum() + "");

            uploadGradeDataBean.setTrRightAnswer(data.getTp_answer());
            uploadGradeDataBean.setTrType(data.getTp_type() + "");
            UploadGrade(uploadGradeDataBean);
            /*if (data.getTp_senum() == ANSWER_QUESTION_SUM) {
                mIsLastQuestion = true;
                // mToResult.setVisibility(View.VISIBLE);
            } else {
                mIsLastQuestion = false;
            }*/
        }
        mConfirmButton.setVisibility(View.GONE);


    }


    /**
     * 2019年4月1日 11:41:27
     * jiao hao kang
     * 单选题
     */
    private void singleChoice(ArrayList<SingleChoiceItemBean> singleDatas) {

        mOptionRecyclerview.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        SingleChoiceRecyclerViewAdapter answerSingleChoiceRecyclerViewAdapter = new SingleChoiceRecyclerViewAdapter(this);
        mOptionRecyclerview.setAdapter(answerSingleChoiceRecyclerViewAdapter);
        answerSingleChoiceRecyclerViewAdapter.setmList(singleDatas);
        answerSingleChoiceRecyclerViewAdapter.setOnItemClickListener(new SingleChoiceRecyclerViewAdapter.onItemClickListener() {
            @Override
            public void onItemClick(SingleChoiceItemBean item) {
                mUserAnswer = item;
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.single_choice_confirm_button:
                if (mUserAnswer != null && !TextUtils.isEmpty(mUserAnswer.getOption())) {
                    confirm();
                } else {
                    Toast.makeText(this, "请选择答案!", Toast.LENGTH_SHORT).show();
                }


                break;

        /*    case R.id.single_choice_to_result_button:
                //toResult(date, System.currentTimeMillis() - submitTime);
                getAnswerRecord();
                break;*/
            default:
                break;
        }
    }

    @Override
    public void websocketStatusChange(int color) {
        Log.i("jiao", "單nwebsocketStatusChange: 前=" + color);
        if (mWebsocket_status != null) {
            mWebsocket_status.setBackgroundResource(color);
            Log.i("jiao", "單nwebsocketStatusChange: 后=" + color);
        }
    }


    private void showDiaLog(int type, String trueAnwer, String UserAnswer) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this, R.style.Translucent_NoTitle).create();
            alertDialog.setCancelable(false);
        }
        alertDialog.show();
        //获取对话框当前的参数值
        android.view.WindowManager.LayoutParams p = alertDialog.getWindow().getAttributes();
        //高度自适应
        p.height = (int) (MATCH_PARENT);
        //宽度自适应
        p.width = (int) (MATCH_PARENT);
        alertDialog.getWindow().setAttributes(p);

        TextView rightAnswer = null;
        TextView uswerAnswer = null;
        Button btnGetRecord = null;
        amswer_results_bg0 = null;
        amswer_results_bg1 = null;
        factory = LayoutInflater.from(this);
        switch (type) {
            case 0:

                view = factory.inflate(R.layout.alertdialog_amswer_results_yes, null);

                alertDialog.setContentView(view);
                amswer_results_bg0 = view.findViewById(R.id.amswer_results_yes_bg0);
                amswer_results_bg1 = view.findViewById(R.id.amswer_results_yes_bg1);
                amswer_results_bg2 = view.findViewById(R.id.amswer_results_yes_bg2);
                rightAnswer = view.findViewById(R.id.dia_textview1_yes);
                uswerAnswer = view.findViewById(R.id.dia_textview2_yes);
                btnGetRecord = view.findViewById(R.id.amswer_results_yes_get_record);
                if (data.getTp_senum() == ANSWER_QUESTION_SUM) {
                    //最后一道题显示最后跳转到获取成绩列表的按钮
                    btnGetRecord.setVisibility(View.VISIBLE);
                } else {
                    btnGetRecord.setVisibility(View.GONE);
                }
                btnGetRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAnswerRecord();
                    }
                });
                if (trueAnwer.length() > 10) {
                    rightAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_18));
                } else {
                    rightAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_36));
                }
                if (UserAnswer.length() > 10) {
                    uswerAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_18));
                } else {
                    uswerAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_36));
                }
                rightAnswer.setText(trueAnwer);
                uswerAnswer.setText(UserAnswer);

                break;

            case 1:

                view = factory.inflate(R.layout.alertdialog_amswer_results_no, null);

                alertDialog.setContentView(view);

                amswer_results_bg0 = view.findViewById(R.id.amswer_results_no_bg0);
                amswer_results_bg1 = view.findViewById(R.id.amswer_results_no_bg1);
                amswer_results_bg2 = view.findViewById(R.id.amswer_results_no_bg2);
                rightAnswer = view.findViewById(R.id.dia_textview1_no);
                uswerAnswer = view.findViewById(R.id.dia_textview2_no);
                btnGetRecord = view.findViewById(R.id.amswer_results_no_get_record);
                if (data.getTp_senum() == ANSWER_QUESTION_SUM) {
                    //最后一道题显示最后跳转到获取成绩列表的按钮
                    btnGetRecord.setVisibility(View.VISIBLE);
                } else {
                    btnGetRecord.setVisibility(View.GONE);
                }
                btnGetRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAnswerRecord();
                    }
                });

                if (trueAnwer.length() > 10) {
                    rightAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_18));
                } else {
                    rightAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_36));
                }
                if (UserAnswer.length() > 10) {
                    uswerAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_18));
                } else {
                    uswerAnswer.setTextSize(this.getResources().getDimension(R.dimen.sp_36));
                }
                rightAnswer.setText(trueAnwer);
                uswerAnswer.setText(UserAnswer);
                break;

            default:
                break;
        }
        //不是最后一道题进行弹窗倒计时切换布局
        if (!mIsLastQuestion) {
            if (handler != null) {
                handler.sendEmptyMessageDelayed(0, 1000);
            }


        } else {
            if (handler != null) {
                handler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    }


    private SingleChoiceActivity.MyHandler handler = new SingleChoiceActivity.MyHandler(SingleChoiceActivity.this);

    //弹窗动画切换弹窗布局倒计时2秒
    private int mAnimationCountDown = 2;


    /**
     * Handler 内部类(防内存泄露)
     */
    private class MyHandler extends Handler {
        // 弱引用 ，防止内存泄露
        private WeakReference<SingleChoiceActivity> weakReference;


        public MyHandler(SingleChoiceActivity handlerMemoryActivity) {
            weakReference = new WeakReference<SingleChoiceActivity>(handlerMemoryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:

                    if (mAnimationCountDown > 0) {
                        mAnimationCountDown--;
                        if (handler != null) {
                            handler.sendEmptyMessageDelayed(0, 1000);
                        }

                    } else {
                        //开始透明渐变切换弹窗布局
                        dialogSwitchoverAnimation(amswer_results_bg0, amswer_results_bg1);
                    }
                    break;
                case 1:
                    if (mAnimationCountDown > 0) {
                        mAnimationCountDown--;
                        if (handler != null) {
                            handler.sendEmptyMessageDelayed(1, 1000);
                        }

                    } else {
                        //开始透明渐变切换弹窗布局
                        dialogSwitchoverAnimation(amswer_results_bg0, amswer_results_bg2);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //弹窗渐变透明动画
    private void dialogSwitchoverAnimation(ConstraintLayout layout0, ConstraintLayout layout1) {

        if (layout0 != null && layout1 != null) {

            ObjectAnimator anim0 = ObjectAnimator.ofFloat(layout0, "alpha", 1f, 0f);
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(layout1, "alpha", 0f, 1f);
            animatorSet = new AnimatorSet();
            animatorSet.playTogether(anim0, anim1);
            animatorSet.setDuration(1 * 1000);
            animatorSet.start();

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }

        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = null;
        }
    }
}
