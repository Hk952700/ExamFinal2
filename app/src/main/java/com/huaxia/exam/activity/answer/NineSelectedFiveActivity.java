package com.huaxia.exam.activity.answer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
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
import com.huaxia.exam.adapter.MultiSelectedMultiOptionRecyclerViewAdapter;
import com.huaxia.exam.adapter.MultiSelectedMultiQuestionRecyclerViewAdapter;
import com.huaxia.exam.base.BaseActivity;
import com.huaxia.exam.bean.AnswerNineSelectedFiveOptionBean;
import com.huaxia.exam.bean.AnswerResultDataBean;
import com.huaxia.exam.bean.UploadGradeDataBean;
import com.huaxia.exam.utils.SharedPreUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.huaxia.exam.utils.AnswerConstants.ANSWER_NINE_SELECTED_FIVE_COUNT;
import static com.huaxia.exam.utils.AnswerConstants.ANSWER_QUESTION_SUM;
import static com.huaxia.exam.utils.AnswerConstants.GET_RECORDE;

/**
 * 2019年4月12日 10:45:48
 * jiao hao kang
 * 九选五 填空题(九宫格)
 */
public class NineSelectedFiveActivity extends BaseActivity implements View.OnClickListener {
    private ArrayList<AnswerNineSelectedFiveOptionBean> bufferArrayList = new ArrayList<>();
    private ArrayList<AnswerNineSelectedFiveOptionBean> optionArrayList = new ArrayList<>();
    //private TextView mCountDownText;
    private RecyclerView mQuestionRecyclerview;
    private ImageView mQuestionRemoveImageview;
    private RecyclerView mOptionRecyclerview;
    private MultiSelectedMultiOptionRecyclerViewAdapter mOptionRecyclerViewAdapter;
    private MultiSelectedMultiQuestionRecyclerViewAdapter mQuestionRecyclerViewAdapter;
    /* private TextView mConfirmText;*/
    // private int count;
    private Button mConfirmButton;
    private String mOptions01;
    private String mAnswer;
    /* private LinearLayout mOver;*/
   /* private TextView mTime;
    private TextView mRightAnswer;
    private TextView mSelectedAnswer;*/
    private AnswerResultDataBean answer;
    /*private Button mToResult;*/
    private long date;
    private long submitTime;

    //private TextView mTitleContext;
    /*private ImageView mOverImage;*/
    private RelativeLayout mWebsocket_status;
    /* private SimpleDraweeView mTitle2Back;*/
    private AlertDialog alertDialog;
    private View view;
    private LayoutInflater factory;
    private TextView mTvUsername;
    private TextView mTvNumberplate;
    private TextView mTvAnswerNum;
    private ImageView mIvCountDownTens;
    private ImageView mIvCountDownOnes;
    private boolean mIsLastQuestion = false;
    private ConstraintLayout amswer_results_bg0;
    private ConstraintLayout amswer_results_bg1;
    private AnimatorSet animatorSet;
    private ConstraintLayout amswer_results_bg2;

    @Override
    public int setContentView() {
        return R.layout.activity_nine_selected_five;
    }

    @Override
    public Context setContext() {
        return NineSelectedFiveActivity.this;
    }

    @Override
    public void init() {
        //右上角WebSocket状态方框
        mWebsocket_status = (RelativeLayout) findViewById(R.id.nine_selected_five_websocket_status);


        //倒计时textview
        //mCountDownText = (TextView) findViewById(R.id.nine_selected_five_count_down);
        //答案的recyclerview
        mQuestionRecyclerview = (RecyclerView) findViewById(R.id.nine_selected_five_question_recyclerview);
        //移除选中的选项
        mQuestionRemoveImageview = (ImageView) findViewById(R.id.nine_selected_five_question_remove_imageview);
        //显示选项的recyclerview
        mOptionRecyclerview = (RecyclerView) findViewById(R.id.nine_selected_five_option_recyclerview);
        //确认图片
        mConfirmButton = (Button) findViewById(R.id.nine_selected_five_confirm_button);


        mTvUsername = (TextView) findViewById(R.id.nine_selected_five_username);
        mTvNumberplate = (TextView) findViewById(R.id.nine_selected_five_numberplate);
        mTvAnswerNum = (TextView) findViewById(R.id.nine_selected_five_answer_num);


        mIvCountDownTens = (ImageView) findViewById(R.id.nine_selected_five_count_down_tens);
        mIvCountDownOnes = (ImageView) findViewById(R.id.nine_selected_five_count_down_ones);

        mConfirmButton.setOnClickListener(this);
        mQuestionRemoveImageview.setOnClickListener(this);


        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        answer = (AnswerResultDataBean) intent.getParcelableExtra("answer");

        if (answer != null) {

            mTvUsername.setText(SharedPreUtils.getString(this, "user_name"));
            mTvAnswerNum.setText(answer.getTp_senum() + "/" + ANSWER_QUESTION_SUM);
            mTvNumberplate.setText(SharedPreUtils.getString(this, "user_numberplate") + "号");


            mOptions01 = answer.getTp_subject();
            mAnswer = answer.getTp_answer();
            if (!TextUtils.isEmpty(mOptions01)) {
                char[] chars = mOptions01.toCharArray();

                for (int i = 0; i < chars.length; i++) {
                    AnswerNineSelectedFiveOptionBean answerNineSelectedFiveOptionBean = new AnswerNineSelectedFiveOptionBean();
                    answerNineSelectedFiveOptionBean.setValue(String.valueOf(chars[i]));
                    answerNineSelectedFiveOptionBean.setIndex(i);
                    optionArrayList.add(answerNineSelectedFiveOptionBean);
                }


            }
        }
        nineSelectedFive(bufferArrayList, optionArrayList);
        startCountDown(mIvCountDownTens, mIvCountDownOnes, 30);

    }


    @Override
    public void onCountDownFinish(long date, long submitTime) {
        super.onCountDownFinish(date, submitTime);
        this.date = date;
        this.submitTime = submitTime;
        if (answer != null) {
            if (answer.getTp_senum() == ANSWER_QUESTION_SUM) {
                mIsLastQuestion = true;
            } else {
                mIsLastQuestion = false;
            }
        }

        UploadGradeDataBean uploadGradeDataBean = new UploadGradeDataBean();

        if (bufferArrayList.size() != 0) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < bufferArrayList.size(); i++) {
                stringBuffer.append(bufferArrayList.get(i).getValue());
            }
            String s = stringBuffer.toString();
            Log.i("jtest", "onCountDownFinish: 九宫格学生答案為:" + s);
            Log.i("jtest", "onCountDownFinish: 正確答案答案為:" + mAnswer);

            if (s.equals(mAnswer)) {
                //对错
                uploadGradeDataBean.setTrRight("0");
                showDiaLog(0, mAnswer, s);

                //分数
                uploadGradeDataBean.setTrMark(answer.getTp_score() + "");
            } else {


                showDiaLog(1, mAnswer, s);
                //对错
                uploadGradeDataBean.setTrRight("1");
                //分数
                uploadGradeDataBean.setTrMark("0");
            }
            Log.i("jtest", "onCountDownFinish: 时间:" + date);

            //学生答案
            uploadGradeDataBean.setTrAnswer(s);

        } else {
            //学生答案
            uploadGradeDataBean.setTrAnswer("未作答");

            showDiaLog(1, mAnswer, "未作答");
            //分数
            uploadGradeDataBean.setTrMark("0");
            //对错
            uploadGradeDataBean.setTrRight("1");
        }


        if (answer != null) {
            //班级
            uploadGradeDataBean.setTrClass(answer.getTp_class() + "");
            //耗时
            uploadGradeDataBean.setTrTime(date + "");
            uploadGradeDataBean.setTrQuestion(answer.getTp_subject());
            //题号
            uploadGradeDataBean.setTrPapernum(answer.getTp_senum() + "");
            uploadGradeDataBean.setTrRightAnswer(answer.getTp_answer());
            uploadGradeDataBean.setTrType(answer.getTp_type() + "");

            UploadGrade(uploadGradeDataBean);
           /* if (answer.getTp_senum() == ANSWER_QUESTION_SUM) {
                mIsLastQuestion = true;
                *//*mToResult.setVisibility(View.VISIBLE);*//*
                //最后一道题显示最后跳转到获取成绩列表的按钮
            } else {
                mIsLastQuestion = false;
            }*/
        }

    }


    /**
     * 2019年4月1日 10:17:54
     * jiao hao kang
     * 九选五(九宫格)
     */
    private void nineSelectedFive(final ArrayList<AnswerNineSelectedFiveOptionBean> bufferArrayList, final ArrayList<AnswerNineSelectedFiveOptionBean> optionArrayList) {


        //设置两个recyclertview的布局管理器  初始化dapter 配置dapter setList
        mOptionRecyclerview.setLayoutManager(new GridLayoutManager(NineSelectedFiveActivity.this, 3));
        mQuestionRecyclerview.setLayoutManager(new LinearLayoutManager(NineSelectedFiveActivity.this, LinearLayoutManager.HORIZONTAL, false));
        mOptionRecyclerViewAdapter = new MultiSelectedMultiOptionRecyclerViewAdapter(NineSelectedFiveActivity.this, 5);
        mQuestionRecyclerViewAdapter = new MultiSelectedMultiQuestionRecyclerViewAdapter(NineSelectedFiveActivity.this, 5);
        mOptionRecyclerview.setAdapter(mOptionRecyclerViewAdapter);
        mQuestionRecyclerview.setAdapter(mQuestionRecyclerViewAdapter);
        //下面选项的
        mOptionRecyclerViewAdapter.setmList(optionArrayList);
        //上面填空的
        mQuestionRecyclerViewAdapter.setmList(bufferArrayList);
        //上面填空的背景

        //选中回调
        mOptionRecyclerViewAdapter.setOnOptionItemselected(new MultiSelectedMultiOptionRecyclerViewAdapter.onOptionItemselected() {

            @Override
            public void optionItemselected(AnswerNineSelectedFiveOptionBean selectedItem) {
                bufferArrayList.add(selectedItem);
                mQuestionRecyclerViewAdapter.setmList(bufferArrayList);
                mOptionRecyclerViewAdapter.setSelectedItemCount(mOptionRecyclerViewAdapter.getSelectedItemCount() + 1);
            }

        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nine_selected_five_question_remove_imageview:
                if (mOptionRecyclerViewAdapter.getSelectedItemCount() > 0 && bufferArrayList.size() > 0) {
                    for (int i = 0; i < optionArrayList.size(); i++) {
                        if (bufferArrayList.get(bufferArrayList.size() - 1).getIndex() == optionArrayList.get(i).getIndex()) {
                            optionArrayList.get(i).setChecked(false);
                            //下面选项的
                            mOptionRecyclerViewAdapter.setmList(optionArrayList);
                            mOptionRecyclerViewAdapter.setSelectedItemCount(mOptionRecyclerViewAdapter.getSelectedItemCount() - 1);
                            //循环遍历 移除  刷新两个适配器
                            bufferArrayList.remove(bufferArrayList.size() - 1);
                            //上面填空的
                            mQuestionRecyclerViewAdapter.setmList(bufferArrayList);
                            break;
                        }
                    }


                }


                break;

            case R.id.nine_selected_five_confirm_button:
                if (bufferArrayList.size() != 0) {
                    if (bufferArrayList.size() == ANSWER_NINE_SELECTED_FIVE_COUNT) {
                        confirm();
                    } else {
                        Toast.makeText(this, "请填写完整!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "请选择答案!", Toast.LENGTH_SHORT).show();
                }
                break;

            /*case R.id.nine_selected_five_to_result_button:
                //toResult(date, System.currentTimeMillis() - submitTime);
                getAnswerRecord();
                break;*/
            default:
                break;
        }
    }

    @Override
    public void websocketStatusChange(int color) {
        Log.i("jiao", "九nwebsocketStatusChange: 前=" + color);
        if (mWebsocket_status != null) {

            mWebsocket_status.setBackgroundResource(color);
            Log.i("jiao", "九websocketStatusChange: 前=" + color);

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
                if (answer.getTp_senum() == ANSWER_QUESTION_SUM) {
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
                if (answer.getTp_senum() == ANSWER_QUESTION_SUM) {
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

    private NineSelectedFiveActivity.MyHandler handler = new NineSelectedFiveActivity.MyHandler(NineSelectedFiveActivity.this);

    //弹窗动画切换弹窗布局倒计时2秒
    private int mAnimationCountDown = 2;


    /**
     * Handler 内部类(防内存泄露)
     */
    private class MyHandler extends Handler {
        // 弱引用 ，防止内存泄露
        private WeakReference<NineSelectedFiveActivity> weakReference;


        public MyHandler(NineSelectedFiveActivity handlerMemoryActivity) {
            weakReference = new WeakReference<NineSelectedFiveActivity>(handlerMemoryActivity);
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
            animatorSet = animatorSet;
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
