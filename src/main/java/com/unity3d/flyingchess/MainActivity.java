package com.unity3d.flyingchess;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unity3d.player.UnityPlayer;

import java.lang.reflect.Array;
import java.util.ArrayList;

import FlyChess.Action;
import FlyChess.AutoAI;
import FlyChess.Chess;
import FlyChess.GameManager;
import FlyChess.Queue;

import static com.unity3d.player.UnityPlayer.*;

public class MainActivity extends UnityPlayerActivity {

    public static final String TAG = "MainActivity";
    //消息类型,必须是static final类型
    private static final int TURN = 1;//设置现在轮到谁
    private static final int REQUESTDICE = 2;//请求扔骰子
    private static final int THROWDICE = 3;//真正扔骰子，播放扔骰子的动画
    private static final int SHOW_THROWDICE_BUTTON = 4;//显示扔骰子的那个按钮
    private static final int SHOW_AVAILABLE_CHESS = 5;//设置可用的棋子
    private static final int EXECUTE_ACTION = 6;//在Unity中执行动作
    private static final int SELECT_CHESS = 7;//AI选择棋子

    public RunThread runThread;
    public LinearLayout unity_view;
    public TextView action_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    //初始化
    //定义组件及创建线程，开启线程
    private void init(){
        unity_view = (LinearLayout)findViewById(R.id.unity_view);
        action_text = (TextView) findViewById(R.id.cur_action);
        //将Unity视图添加到Unity_View中
        View mView = mUnityPlayer.getView();
        unity_view.addView(mView);
        //unity_view.requestFocus();

        //创建四人游戏，且都是AI
        runThread = new RunThread(new GameManager(new AutoAI(Chess.RED), new AutoAI(Chess.YELLOW),
                new AutoAI(Chess.BLUE), new AutoAI(Chess.GREEN)));
        //进程开始执行
        runThread.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //现在轮到谁了TURN
                case TURN:{
                    int turn = msg.getData().getInt("turn");
                    //更改UI
                    action_text.setText("现在轮到 ： " + Integer.toString(turn));
                    break;
                }
                case REQUESTDICE:{
                    //Unity视图所有人物设为不可选
                    break;
                }
                case THROWDICE:{
                    int dice = msg.getData().getInt("dice");
                    action_text.setText("骰子结果为 ： " + Integer.toString(dice));
                    break;
                }
                case SHOW_THROWDICE_BUTTON:{
                    break;
                }
                case SHOW_AVAILABLE_CHESS:{
                    //获取可选棋子列表，显示可选棋子
                    break;
                }
                case SELECT_CHESS:{
                    int choice = msg.getData().getInt("choice");
                    //更改UI
                    action_text.setText("玩家选择为 ： " + Integer.toString(choice));
                    break;
                }
                case EXECUTE_ACTION:{
                    String cur_action = msg.getData().getString("cur_action");
                    String cur_action_list = msg.getData().getString("cur_action_list");
                    //显示当前动作
                    action_text.setText(cur_action);
                    //将动作指令字符串传给Unity
                    UnitySendMessage("ChessManager", "executeAction", cur_action_list);
                    break;
                }
            }
        }
    };

    class RunThread extends Thread{
        public GameManager gameManager;

        public RunThread(GameManager gameManager) {
            this.gameManager = gameManager;
        }

        @Override
        public void run() {

            //游戏结束线程停止
            while(!gameManager.isGameOver()) {
                //首先将轮次显示在屏幕上
                showTurn();

                //请求骰子
                requestDice();
                //检查是否已经返回骰子结果
                while (gameManager.waitDice()){
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //请求选定棋子
                requestChess();
                //检查是否已经返回骰子结果
                while (gameManager.waitChoice()){
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //对于取消托管很重要的一件事，AI的睡眠必须放在这里
                //因为有时间差，睡眠放在requestDice里面会出问题的
                //原因自己思考...

                if(gameManager.isAI()) {
                    //睡眠一下，免得AI的动作太快...
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //既然已经选好了棋子，就可以根据action指令集进行操作了
                executeAction();
            }
        }

        //显示轮次
        private void showTurn(){
            Message message = new Message();
            message.what = TURN;
            Bundle data = new Bundle();
            data.putInt("turn", gameManager.getTurn());
            message.setData(data);
            handler.sendMessage(message);
        }

        //随机产生骰子
        public int newDice(){
            return ((int)(Math.random()*1000000))%6 + 1;
        }

        //请求投掷一个骰子
        private void requestDice(){
            //先判断是人还是AI
            //请求骰子时：
            //step 1:设置所有的棋子不能被选择
            //step 2:扔骰子的按钮以及动画
            Message message = new Message();
            message.what = REQUESTDICE;
            handler.sendMessage(message);

            //如果是AI
            if(gameManager.isAI()){
                //AI主动投骰子
                int dice = newDice();
                //播放投骰子动画
                message = new Message();
                message.what = THROWDICE;
                Bundle data = new Bundle();
                data.putInt("dice", dice);
                message.setData(data);
                handler.sendMessage(message);

                //这里可以直接调用Unity的投骰子接口，显示动画并返回骰子结果
                //直接使用dice传参

                //延长一下时间，让AI别那么快
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                gameManager.setDice(dice);
            }
            //如果是人
            //如果是人，则应该出现投掷的按钮，并让玩家自己选择
            else{
                Log.d(TAG, "UnExpected Dice");
            }
        }

        //请求选定一个棋子
        private void requestChess(){
            //请求选择棋子时：
            //step1:向handler发送可选棋子；
            Message message = new Message();
            message.what = SHOW_AVAILABLE_CHESS;
            Bundle data = new Bundle();
            //data.putString("chessList","123");
            message.setData(data);
            handler.sendMessage(message);

            //如果是AI
            //step2:AI直接选择棋子,向handler发送选择;
            //step3:在manager中设置选择
            if(gameManager.isAI()){



                //通过函数自主选择
                int choice = gameManager.getAIChoice();
                //发送选择
                message = new Message();
                message.what = SELECT_CHESS;
                data = new Bundle();
                data.putInt("choice", choice);
                message.setData(data);
                handler.sendMessage(message);

                //延长一下时间，让AI别那么快
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                gameManager.setChoice(choice);
            }
            //如果是人
            else{
                Log.d(TAG, "UnExpected Chess");
            }
        }

        //生成动作指令集，执行动作
        private void executeAction(){
            //递归发送UI消息

            Queue<Action> actionlist = gameManager.actionlist();
            for(Action action:actionlist) {
                Message message = new Message();
                message.what = EXECUTE_ACTION;
                Bundle data = new Bundle();
                String cur_action = action.toString();
                String cur_action_list = action.toActionString();
                data.putString("cur_action", cur_action);
                data.putString("cur_action_list", cur_action_list);
                message.setData(data);
                handler.sendMessage(message);
            }
        }
    }



    //转换函数
    private static String diceToString(int dice)
    {
        String str = "播放骰子动画:";
        if(dice == 1) return str + "点数一";
        else if(dice == 2) return str + "点数二";
        else if(dice == 3) return str + "点数三";
        else if(dice == 4) return str + "点数四";
        else if(dice == 5) return str + "点数五";
        else if(dice == 6) return str + "点数六";
        else return "点数错乱.";
    }
    private static int buttonColor(int player)
    {
        if(player == Chess.RED) return Color.RED;
        else if(player == Chess.YELLOW) return Color.YELLOW;
        else if(player == Chess.BLUE) return Color.BLUE;
        else return Color.GREEN;
    }
}
