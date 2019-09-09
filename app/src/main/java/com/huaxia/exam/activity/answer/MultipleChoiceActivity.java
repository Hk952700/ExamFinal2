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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxia.exam.R;
import com.huaxia.exam.adapter.MultipleChoiceRecyclerViewAdapter;
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
 * 多选Activity
 */
public class MultipleChoiceActivity extends BaseActivity implements View.OnClickListener {

    //private TextView mCountDown;
    private TextView mQuestion;
    private RecyclerView mOptionRecyclerview;
    private Button mConfirmButton;

    private ArrayList<SingleChoiceItemBean> optionsArray = new ArrayList<>();
    private AnswerResultDataBean data;

    private long date;
    private long submitTime;

    private RelativeLayout mWebsocket_status;

    private AlertDialog alertDialog;
    private LayoutInflater factory;
    private View view;
    private String[] split;
    private StringBuffer stringBuffer2;
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
        return R.layout.activity_multiple_choice;
    }

    @Override
    public Context setContext() {
        return MultipleChoiceActivity.this;
    }

    @Override
    public void init() {
        //右上角WebSocket状态方框
        mWebsocket_status = (RelativeLayout) findViewById(R.id.multiple_choice_websocket_status);


        //倒计时显示textview
        //mCountDown = (TextView) findViewById(R.id.multiple_choice_count_down);
        //问题展示
        mQuestion = (TextView) findViewById(R.id.multiple_choice_question);
        //选项展示
        mOptionRecyclerview = (RecyclerView) findViewById(R.id.multiple_choice_option_recyclerview);
        //确认图片
        mConfirmButton = (Button) findViewById(R.id.multiple_choice_confirm_button);


        mTvUsername = (TextView) findViewById(R.id.multiple_choice_username);
        mTvNumberplate = (TextView) findViewById(R.id.multiple_choice_numberplate);
        mTvAnswerNum = (TextView) findViewById(R.id.multiple_choice_answer_num);

        mIvCountDownTens = (ImageView) findViewById(R.id.multiple_choice_count_down_tens);
        mIvCountDownOnes = (ImageView) findViewById(R.id.multiple_choice_count_down_ones);

        initDataAndRecycler();
    }


    private void initDataAndRecycler() {
        Intent intent = getIntent();
        data = (AnswerResultDataBean) intent.getParcelableExtra("answer");
        Log.i("jtest", "initDataAndRecycler: 多选题Activity接受到intent=" + data);
        //TODO 后期完善
        if (data != null) {
            if (data.getTp_subject().length() < 13) {
                mQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            } else {
                mQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            }


            mTvUsername.setText(SharedPreUtils.getString(this, "user_name"));
            mTvNumberplate.setText(SharedPreUtils.getString(this, "user_numberplate") + "号");
            mTvAnswerNum.setText(data.getTp_senum() + "/" + ANSWER_QUESTION_SUM);


            mQuestion.setText(data.getTp_subject().trim());
            //选项
            ArrayList<SingleChoiceItemBean> multipleChoiceItemBeans = new ArrayList<>();
            split = data.getTp_options().split("/");
            for (int i = 0; i < split.length; i++) {
                char[] chars = split[i].toCharArray();
                StringBuffer stringBuffer = new StringBuffer();
                for (int j = 0; j < chars.length; j++) {
                    if (j > 0) {
                        stringBuffer.append(chars[j]);
                    }
                }

                multipleChoiceItemBeans.add(new SingleChoiceItemBean(String.valueOf(chars[0]), stringBuffer.toString(), false, false));
            }

            multipleChoice(multipleChoiceItemBeans);
        }
        startCountDown(mIvCountDownTens, mIvCountDownOnes, 20);
        mConfirmButton.setOnClickListener(this);

    }

    /**
     * 2019年5月6日 18:05:45
     * jiao hao kang
     *
     * @param multipleChoiceItemBeans
     */
    private void multipleChoice(ArrayList<SingleChoiceItemBean> multipleChoiceItemBeans) {

        MultipleChoiceRecyclerViewAdapter multipleChoiceRecyclerViewAdapter = new MultipleChoiceRecyclerViewAdapter(MultipleChoiceActivity.this);
        mOptionRecyclerview.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        mOptionRecyclerview.setAdapter(multipleChoiceRecyclerViewAdapter);
        multipleChoiceRecyclerViewAdapter.setmList(multipleChoiceItemBeans);
        multipleChoiceRecyclerViewAdapter.setOnItemClickListener(new MultipleChoiceRecyclerViewAdapter.onItemClickListener() {
            @Override
            public void onItemClick(ArrayList<SingleChoiceItemBean> items) {
                optionsArray = items;
            }
        });


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


        if (data != null) {
            char[] chars = data.getTp_answer().trim().toCharArray();
            stringBuffer2 = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                for (int j = 0; j < chars.length; j++) {
                    if (split[i].trim().substring(0, 1).equals(String.valueOf(chars[j]))) {
                        stringBuffer2.append(String.valueOf(chars[j]) + ":" + split[i].trim().substring(1, split[i].trim().length()) + ",");

                        continue;
                    }
                }
            }


            stringBuffer2 = new StringBuffer(stringBuffer2.substring(0, stringBuffer2.length() - 1));
        }

        if (optionsArray != null && optionsArray.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            StringBuffer stringBuffer1 = new StringBuffer();
            for (int i = 0; i < optionsArray.size(); i++) {
                stringBuffer.append(optionsArray.get(i).getOption().trim());
                stringBuffer1.append(optionsArray.get(i).getOption() + ":" + optionsArray.get(i).getContext().trim() + ",");
            }
            String substring = stringBuffer1.substring(0, stringBuffer1.length() - 1);

            if (isRight(stringBuffer.toString(), data.getTp_answer())) {


                //对错
                uploadGradeDataBean.setTrRight("0");
                showDiaLog(0, stringBuffer2.toString(), substring);
                //分数
                uploadGradeDataBean.setTrMark(data.getTp_score() + "");
            } else {

                showDiaLog(1, stringBuffer2.toString(), substring);
                //对错
                uploadGradeDataBean.setTrRight("1");
                //分数
                uploadGradeDataBean.setTrMark("0");
            }
            //学生答案
            uploadGradeDataBean.setTrAnswer(stringBuffer.toString());

        } else {
            //学生答案
            uploadGradeDataBean.setTrAnswer("未作答");

            showDiaLog(1, stringBuffer2.toString(), "未作答");
            //对错
            uploadGradeDataBean.setTrRight("1");
            //分数
            uploadGradeDataBean.setTrMark("0");
        }

        uploadGradeDataBean.setTrQuestion(data.getTp_subject());
        //班级
        uploadGradeDataBean.setTrClass(data.getTp_class() + "");
        //耗时
        uploadGradeDataBean.setTrTime(date + "");
        //题号
        uploadGradeDataBean.setTrPapernum(data.getTp_senum() + "");
        uploadGradeDataBean.setTrRightAnswer(data.getTp_answer());

        uploadGradeDataBean.setTrType(data.getTp_type() + "");
        UploadGrade(uploadGradeDataBean);
      /*  if (data.getTp_senum() == ANSWER_QUESTION_SUM) {
            mIsLastQuestion = true;
            //mToResult.setVisibility(View.VISIBLE);
        } else {
            mIsLastQuestion = false;
        }*/

        mConfirmButton.setVisibility(View.GONE);

    }


    /**
     * 2019年5月6日 18:38:42
     * jiao hao kang
     *
     * @param userAnswer 选手答案
     * @param answer     正确答案
     * @return 是否正确
     */

    private boolean isRight(String userAnswer, String answer) {

        char[] chars0 = userAnswer.toCharArray();//选手答案
        char[] chars1 = answer.trim().toCharArray();//正确答案

        if (chars0.length != chars1.length) {
            return false;
        } else {
            for (int i = 0; i < chars1.length; i++) {
                boolean flag = false;
                for (int j = 0; j < chars0.length; j++) {
                    if (String.valueOf(chars0[j]).equals(String.valueOf(chars1[i]))) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    return false;
                }

            }
        }

        return true;
    }


    @Override
    public void websocketStatusChange(int color) {
        Log.i("jiao", "多nwebsocketStatusChange: 前=" + color);
        if (mWebsocket_status != null) {
            mWebsocket_status.setBackgroundResource(color);
            Log.i("jiao", "多nwebsocketStatusChange: 后=" + color);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multiple_choice_confirm_button:
                if (optionsArray != null && optionsArray.size() > 0) {
                    confirm();
                } else {
                    Toast.makeText(this, "请选择答案!", Toast.LENGTH_SHORT).show();
                }
                break;

          /*  case R.id.multiple_choice_to_result_button:
                //toResult(date, System.currentTimeMillis() - submitTime);//最后一道获取排名
                getAnswerRecord();

                break;*/
            default:
                break;
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

    private MultipleChoiceActivity.MyHandler handler = new MultipleChoiceActivity.MyHandler(MultipleChoiceActivity.this);

    //弹窗动画切换弹窗布局倒计时2秒
    private int mAnimationCountDown = 2;


    /**
     * Handler 内部类(防内存泄露)
     */
    private class MyHandler extends Handler {
        // 弱引用 ，防止内存泄露
        private WeakReference<MultipleChoiceActivity> weakReference;


        public MyHandler(MultipleChoiceActivity handlerMemoryActivity) {
            weakReference = new WeakReference<MultipleChoiceActivity>(handlerMemoryActivity);
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
