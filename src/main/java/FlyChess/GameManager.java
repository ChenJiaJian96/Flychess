package FlyChess;

/**
 * Created by gitfan on 3/29/18.
 */

//单机版GameManager
public class GameManager {

    private BasicAI player[];
    private Chess chessboard[];
    private boolean waitingdice;//还在等待扔骰子
    private boolean waitingchoice;//还在等待选择

    private int dice;//当前的骰子
    private int turn;//当前轮到谁？
    private int choice;//当前的选择是什么?

    public GameManager(BasicAI player1, BasicAI player2, BasicAI player3, BasicAI player4) {
        player = new BasicAI[4];
        player[0] = player1;
        player[1] = player2;
        player[2] = player3;
        player[3] = player4;
        chessboard = new Chess[72];
        for(int i = 0 ; i < 72; i++)
        {
            chessboard[i] = new Chess(i);
        }
        dice = -1;
        choice = -1;
        turn = 0;

        waitingdice = true;
        waitingchoice = true;

    }
    public boolean isGameOver() {
        for(int i = 0 ; i < 4 ;i++){
            if(player[i].isFinish()){
                return true;
            }
        }
        return false;
    }
    //提供给扔骰子的按钮，按按钮时调用这个函数
    public void setDice(int dice){
        if(dice <= 0 || dice > 6){
            System.out.print("dice out of range in GameManager: setDice(int dice)");
            System.exit(0);
        }

        waitingdice = false;//玩家已经投掷骰子了
        this.dice = dice;
        //扔完骰子后开始等待玩家选择棋子
        this.waitingchoice = true;
    }
    //提供给点击棋子的按钮，玩家点击棋子时调用这个函数
    //用来设置当前选择的棋子,如果没有棋子可以选择请使用setChoice(-1)
    public void setChoice(int choice) {
        //扔了骰子才可以进行选择
        if(waitingdice){
            System.out.println("unexpected error in GameManager: setChoice(int choice)");
            System.exit(0);
        }

        waitingchoice = false;//取消等待选择的标记
        this.choice = choice;

        //为下一轮游戏做准备
        waitingdice = true;
    }
    //检测骰子是否已经准备好
    public boolean waitDice()
    {
        return waitingdice;
    }
    //检测玩家是否已经选择棋子
    public boolean waitChoice()
    {
        return waitingchoice;
    }

    //返回当前玩家可以选择的棋子
    //前提：扔了骰子才可以调用
    public Queue<Integer> getChessAvailable() {
        if(waitingdice){
            System.out.println("Unexpected error in GameManager: getChessAvailable(int playerid)");
            System.exit(0);
        }
        PlayerAI playerAI = (PlayerAI) player[getTurn()];
        return playerAI.available_choice(dice);
    }
    //设置玩家为挂机模式
    public void switchToAI(int playerid) {
        //只有玩家模式才可以转换位挂机
        if(player[playerid].getKind() != BasicAI.PEOPLE)
        {
            System.out.println("Unexpected error in GameManager: switchToAI(int playerid)");
            System.exit(0);
        }

        PlayerAI playerAI = (PlayerAI) player[playerid];
        playerAI.switchToAI();
        player[playerid] = playerAI;
    }
    //从挂机模式中恢复
    public void switchToUser(int playerid) {
        //只有玩家AI模式才可以恢复为玩家模式，全自动AI不可以切换为玩家模式
        if(player[playerid].getKind() != BasicAI.PLAYERAI)
        {
            System.out.println("Unexpected error in GameManager: switchToUser(int playerid)");
            System.exit(0);
        }

        PlayerAI playerAI = (PlayerAI) player[playerid];
        playerAI.switchToUser();
        player[playerid] = playerAI;
    }
    //AI自己选择棋子
    public int getAIChoice() {
        //只有扔了骰子AI才可以自动选择
        if(waitingdice){
            System.out.println("Unexpected error in GameManager: getAIChoice(int playerid)");
            System.exit(0);
        }
        return player[getTurn()].ai_choice(dice,chessboard);
    }

    //联机部分可能用到
    //主要提供给UI界面，获取现在轮到谁玩游戏
    //UI那里可以根据这个函数确定轮到谁玩游戏
    //然后设置相关的界面（比如扔骰子，只有轮到的人才出现那个可以出现扔骰子的按钮）
    public int getTurn()
    {
        return turn;
    }

    //判断现在是不是AI在玩游戏
    //还是提供给UI界面，主要是给UI界面用来确定扔骰子时是自动扔，还是等人点击才扔?
    public boolean isAI() {
        return (player[getTurn()].getKind() == BasicAI.AUTOAI || player[getTurn()].getKind() == BasicAI.PLAYERAI );
    }

    //用户选完棋子后，产生的一系列动作
    public Queue<Action> actionlist() {
        //只有选了棋子才能发生动作
        if(waitingchoice)
        {
            System.out.println("Unexpected error in GameManager: actionlist()");
            System.exit(0);
        }


        Queue<Action> queue = new Queue<Action>();
        Action action;
        Chess chess;
        int playerid = getTurn();
        int chessindex = choice;
        if(chessindex < 0)
        {
            turn  = (turn + 1)%4;
            return  queue;
        }
        else
        {
            chess = new Chess(player[playerid].getChess(chessindex));
            //位于停机坪
            if(chess.getStatus() == Chess.STATUS_AIRPORT)
            {
                if(dice >= 5 && dice <= 6)
                {
                    chess.setStatus(Chess.STATUS_STARTLINE);
                    chess.setPos(Chess.originPos[chess.getColor()]);
                    chess.clearIndexList();
                    chess.insertToIndexList(new Pair(playerid,chessindex));

                    //记得更新棋盘或者玩家的棋子(自己的棋子或者他人的棋子)

                    player[playerid].setChess(chessindex,chess);//更新玩家棋子，当前棋盘无影响，无需更新棋盘

                    action = new Action(playerid,chessindex, Action.MOVE_TO_STARTLINE);
                    queue.enqueue(action);

                    if(dice == 5){
                        turn = (turn + 1) % 4;//轮到下一个人
                        return queue;
                    }
                    else
                    {
                        return queue;//不用改变turn，还是这个人
                    }
                }
                else
                {
                    turn  = (turn + 1)%4;
                    return  queue;
                }
            }
            //位于起飞点
            else if(chess.getStatus() == Chess.STATUS_STARTLINE)
            {
                chess.setStatus(Chess.STATUS_FLYING);
                chess.setPos((chess.getPos() + dice) % 52);

                //初步移动
                action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice);
                queue.enqueue(action);


                //可以和自己人合体
                //记得更新自己的棋子和棋盘棋子
                if(chess.mergeTest(chessboard[chess.getPos()]))
                {
                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                    {
                        //插入自己的棋子列表
                        chess.insertToIndexList(pair);
                        //记得隐藏其他的棋子
                        player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                        action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                        queue.enqueue(action);
                    }
                    //更新棋盘
                    chessboard[chess.getPos()] = new Chess(chess);
                    //更新自己的棋子
                    player[playerid].setChess(chessindex,chess);
                }
                //可以吃掉其他玩家
                //记得更新别的玩家的棋子，自己的棋子，以及棋盘
                else if(chess.eatTest(chessboard[chess.getPos()]))
                {
                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                    {
                        //坠落，回到停机坪
                        player[pair.playerId].chesslist[pair.chessId].setFallen();
                        action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                        queue.enqueue(action);
                    }

                    //更新棋盘
                    chessboard[chess.getPos()] = new Chess(chess);
                    //更新自己的棋子
                    player[playerid].setChess(chessindex,chess);
                }
                //很普通的一步，没有合并或者吃掉，但是有可能是跳步
                //记得更新自己的棋子和棋盘
                else
                {
                    //更新棋盘
                    chessboard[chess.getPos()] = new Chess(chess);
                    //更新自己的棋子
                    player[playerid].setChess(chessindex,chess);
                }

                //正常步
                if(!chess.isLucky())
                {
                    if(dice != 6) turn = (turn + 1)%4;
                    return queue;
                }
                //跳步！！！
                //位于起飞点的棋子不可能会有飞步
                else
                {
                    //有可能合并或者吃掉，但不能再连跳

                    //跳之后记得清空跳之前的位置的棋盘

                    //清空之前的棋子
                    chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                    chessboard[chess.getPos()].clearIndexList();

                    chess.setPos((chess.getPos() + 4)%52);

                    //移动
                    action = new Action(playerid,chessindex, Action.QUICK_MOVE,4);
                    queue.enqueue(action);

                    //可以和自己人合体
                    //记得更新自己的棋子和棋盘棋子
                    if(chess.mergeTest(chessboard[chess.getPos()]))
                    {
                        for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                        {
                            //插入自己的棋子列表
                            chess.insertToIndexList(pair);
                            //记得隐藏其他的棋子
                            player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                            action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                            queue.enqueue(action);
                        }
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                        //更新自己的棋子
                        player[playerid].setChess(chessindex,chess);
                    }
                    //可以吃掉其他玩家
                    //记得更新别的玩家的棋子，自己的棋子，以及棋盘
                    else if(chess.eatTest(chessboard[chess.getPos()]))
                    {
                        for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                        {
                            //坠落，回到停机坪
                            player[pair.playerId].chesslist[pair.chessId].setFallen();
                            action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                            queue.enqueue(action);
                        }

                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                        //更新自己的棋子
                        player[playerid].setChess(chessindex,chess);
                    }
                    //很普通的一步，没有合并或者吃掉，但是有可能是跳步
                    //记得更新自己的棋子和棋盘
                    else
                    {
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                        //更新自己的棋子
                        player[playerid].setChess(chessindex,chess);
                    }

                    if(dice != 6) turn = (turn + 1)%4;
                    return queue;
                }

            }
            //位于飞行途中
            //注意！！！棋子还没有正式移动！！！！
            else if(chess.getStatus() == Chess.STATUS_FLYING)
            {

                //是否接近终点线？？？
                if(chess.presprint(dice))
                {
                    //是否可以合并自己人？？
                    //是否可以直接到达终点？？

                    //记得清除原来的棋盘的位置

                    chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                    chessboard[chess.getPos()].clearIndexList();

                    int lastposition = chess.getPos();


                    chess.setEndLine(dice);

//                    //移动
//                    action = new Action(playerid,chessindex,Action.NORMAL_MOVE,dice);
//                    queue.enqueue(action);

                    if(lastposition != chess.getEntry())
                    {
                        action = new Action(playerid,chessindex, Action.NORMAL_MOVE,chess.getEntry() - lastposition);
                        queue.enqueue(action);
                        action = new Action(playerid,chessindex, Action.TURNRIGHT);
                        queue.enqueue(action);
                        action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice - (chess.getEntry() - lastposition));
                        queue.enqueue(action);
                    }
                    //刚刚好在入口就不用转身了，直接走就可以了
                    else
                    {
                        action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice);
                        queue.enqueue(action);
                    }

                    if(chess.getStatus() == Chess.STATUS_FINISH)
                    {
                        for(Pair pair:chess.getIndexlist())
                        {
                            player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_FINISH);
                            action = new Action(pair.playerId,pair.chessId, Action.FINISHED);
                            queue.enqueue(action);
                        }
                    }
                    //还要考虑是否合并自己人
                    else
                    {
                        //可以和自己人合体
                        //记得更新自己的棋子和棋盘棋子
                        if(chess.mergeTest(chessboard[chess.getPos()]))
                        {
                            for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                            {
                                chess.insertToIndexList(pair);

                                player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                queue.enqueue(action);
                            }
                            //更新棋盘
                            chessboard[chess.getPos()] = new Chess(chess);
                            //更新自己的棋子
                            player[playerid].setChess(chessindex,chess);
                        }
                        //记得更新自己的棋子和棋盘
                        else
                        {
                            //更新棋盘
                            chessboard[chess.getPos()] = new Chess(chess);
                            //更新自己的棋子
                            player[playerid].setChess(chessindex,chess);
                        }
                    }

                    if(dice != 6) turn = (turn + 1)%4;
                    return queue;

                }
                //是否已经进入终点线
                else if(chess.sprint())
                {

                    chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                    chessboard[chess.getPos()].clearIndexList();

                    int lastpos = chess.getPos();

                    //反弹~~~
                    boolean rebounded = chess.rebound(dice);


                    //记得添加action
                    //需要考虑直接到达终点,直接到达终点不用考虑反弹
                    //否则需要考虑反弹并且需要考虑是否合并

                    if(chess.getStatus() == Chess.STATUS_FINISH)
                    {
                        action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice);
                        queue.enqueue(action);
                        for(Pair pair:chess.getIndexlist())
                        {
                            player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_FINISH);
                            action = new Action(pair.playerId,pair.chessId, Action.FINISHED);
                            queue.enqueue(action);
                        }
                    }
                    else
                    {
                        //如果反弹
                        if(rebounded)
                        {
                            int endpoint = chess.endPoint();

                            action = new Action(playerid,chessindex, Action.NORMAL_MOVE,endpoint - lastpos);
                            queue.enqueue(action);
                            action = new Action(playerid,chessindex, Action.REVERSE);
                            queue.enqueue(action);
                            action = new Action(playerid,chessindex, Action.NORMAL_MOVE,endpoint - chess.getPos());
                            queue.enqueue(action);
                            action = new Action(playerid,chessindex, Action.REVERSE);
                            queue.enqueue(action);
                        }
                        //否则
                        else
                        {
                            action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice);
                            queue.enqueue(action);
                        }


                        //可以和自己人合体
                        //记得更新自己的棋子和棋盘棋子
                        if(chess.mergeTest(chessboard[chess.getPos()]))
                        {
                            for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                            {
                                chess.insertToIndexList(pair);

                                player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                queue.enqueue(action);
                            }
                            //更新棋盘
                            chessboard[chess.getPos()] = new Chess(chess);
                            //更新自己的棋子
                            player[playerid].setChess(chessindex,chess);
                        }
                        //记得更新自己的棋子和棋盘
                        else
                        {
                            //更新棋盘
                            chessboard[chess.getPos()] = new Chess(chess);
                            //更新自己的棋子
                            player[playerid].setChess(chessindex,chess);
                        }
                    }

                    if(dice != 6) turn = (turn + 1)%4;
                    return queue;
                }
                //普通线路
                else
                {
                    //先清除以前的棋盘
                    chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                    chessboard[chess.getPos()].clearIndexList();


                    //基础移动
                    chess.setPos((chess.getPos() + dice) % 52);
                    action = new Action(playerid,chessindex, Action.NORMAL_MOVE,dice);
                    queue.enqueue(action);


                    //可以和自己人合体
                    //记得更新自己的棋子和棋盘棋子
                    if(chess.mergeTest(chessboard[chess.getPos()]))
                    {
                        for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                        {
                            //插入自己的棋子列表
                            chess.insertToIndexList(pair);
                            //记得隐藏其他的棋子
                            player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                            action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                            queue.enqueue(action);
                        }
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                    }
                    //可以吃掉其他玩家
                    //记得更新别的玩家的棋子，自己的棋子，以及棋盘
                    else if(chess.eatTest(chessboard[chess.getPos()]))
                    {
                        for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                        {
                            //坠落，回到停机坪
                            player[pair.playerId].chesslist[pair.chessId].setFallen();
                            action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                            queue.enqueue(action);
                        }
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                    }
                    else
                    {
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                    }


                    //正常步
                    if(!chess.isLucky())
                    {
                        //更新棋盘
                        chessboard[chess.getPos()] = new Chess(chess);
                        //更新自己的棋子
                        player[playerid].setChess(chessindex,chess);

                    }
                    else
                    {
                        boolean flag = false;

                        //先特判一下是不是可以导致飞步的跳步；
                        if(chess.getPos() == chess.getPreFlyingPoint())
                        {
                            flag = true;//可以导致飞步的跳步

                            //先清除以前的棋盘
                            chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                            chessboard[chess.getPos()].clearIndexList();

                            //再移动
                            chess.setPos((chess.getPos() + 4)%52);
                            action = new Action(playerid,chessindex, Action.QUICK_MOVE,4);
                            queue.enqueue(action);


                            //可以和自己人合体
                            //记得更新自己的棋子和棋盘棋子
                            if(chess.mergeTest(chessboard[chess.getPos()]))
                            {
                                for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                {
                                    //插入自己的棋子列表
                                    chess.insertToIndexList(pair);
                                    //记得隐藏其他的棋子
                                    player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                    action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                    queue.enqueue(action);
                                }
                                //更新棋盘
                                chessboard[chess.getPos()] = new Chess(chess);
                            }
                            //可以吃掉其他玩家
                            //记得更新别的玩家的棋子，自己的棋子，以及棋盘
                            else if(chess.eatTest(chessboard[chess.getPos()]))
                            {
                                for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                {
                                    //坠落，回到停机坪
                                    player[pair.playerId].chesslist[pair.chessId].setFallen();
                                    action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                                    queue.enqueue(action);
                                }
                                //更新棋盘
                                chessboard[chess.getPos()] = new Chess(chess);
                            }
                            else
                            {
                                //更新棋盘
                                chessboard[chess.getPos()] = new Chess(chess);
                            }
                        }

                        //要先先判断飞步，因为飞步也是跳步的一种
                        if(chess.isSuperLucky())
                        {
                            //飞步

                            //是否可以攻击别人？？？

                            //飞步前要先旋转！！！！
                            action = new Action(playerid,chessindex, Action.TURNRIGHT);
                            queue.enqueue(action);

                             int attackpos = chess.getAttackPos();
                             if(chess.attackTest(chessboard[attackpos]))
                             {
                                 action = new Action(playerid,chessindex, Action.QUICK_MOVE,3);
                                 queue.enqueue(action);

                                 //记得修改玩家的棋子和棋盘
                                 for(Pair pair:chessboard[attackpos].getIndexlist())
                                 {
                                     //坠落，回到停机坪
                                     player[pair.playerId].chesslist[pair.chessId].setFallen();
                                     action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                                     queue.enqueue(action);
                                 }

                                 //清空中间的棋盘
                                 chessboard[attackpos].setStatus(Chess.STATUS_EMPTY);
                                 chessboard[attackpos].clearIndexList();


                                 //踢完人继续走
                                 action = new Action(playerid,chessindex, Action.QUICK_MOVE,3);
                                 queue.enqueue(action);
                             }
                             else
                             {
                                 //直接飞过对面
                                 action = new Action(playerid,chessindex, Action.QUICK_MOVE,6);
                                 queue.enqueue(action);
                             }

                            //先清除棋子以前的棋盘
                            chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                            chessboard[chess.getPos()].clearIndexList();
                            //再前进
                            chess.setPos(chess.getFlyingPoint());

                            //有人吗？？自己人还是别人？

                            //合体
                            if(chess.mergeTest(chessboard[chess.getPos()]))
                            {

                                for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                {
                                    //插入自己的棋子列表
                                    chess.insertToIndexList(pair);
                                    //记得隐藏其他的棋子
                                    player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                    action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                    queue.enqueue(action);
                                }
                            }
                            //吃掉
                            else if(chess.eatTest(chessboard[chess.getPos()]))
                            {
                                for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                {
                                    //坠落，回到停机坪
                                    player[pair.playerId].chesslist[pair.chessId].setFallen();
                                    action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                                    queue.enqueue(action);
                                }
                            }

                            //右转
                            action = new Action(playerid,chessindex, Action.TURNRIGHT);
                            queue.enqueue(action);

                            if(!flag)
                            {

                                //直接删除棋盘，因为还要继续跳
                                chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                                chessboard[chess.getPos()].clearIndexList();


                                //在走四步
                                //会有人吗？
                                //会是自己人吗，还是其他人

                                chess.setPos((chess.getPos() + 4)%52);
                                action = new Action(playerid,chessindex, Action.QUICK_MOVE,4);
                                queue.enqueue(action);


                                //合体
                                if(chess.mergeTest(chessboard[chess.getPos()]))
                                {
                                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                    {
                                        //插入自己的棋子列表
                                        chess.insertToIndexList(pair);
                                        //记得隐藏其他的棋子
                                        player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                        action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                        queue.enqueue(action);
                                    }
                                }
                                //吃掉
                                else if(chess.eatTest(chessboard[chess.getPos()]))
                                {
                                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                    {
                                        //坠落，回到停机坪
                                        player[pair.playerId].chesslist[pair.chessId].setFallen();
                                        action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                                        queue.enqueue(action);
                                    }
                                }

                                //更新棋盘
                                chessboard[chess.getPos()] = new Chess(chess);
                                //更新自己的棋子
                                player[playerid].setChess(chessindex,chess);
                            }
                        }
                        else
                        {
                            //如果不在entry才可以跳步，否则就会超过entry了
                            if(chess.getPos() != chess.getEntry())
                            {
                                //普通跳步
                                chessboard[chess.getPos()].setStatus(Chess.STATUS_EMPTY);
                                chessboard[chess.getPos()].clearIndexList();

                                //前进
                                //前面有人吗？
                                //自己人还是别人？

                                chess.setPos((chess.getPos() + 4)%52);
                                action = new Action(playerid,chessindex, Action.QUICK_MOVE,4);
                                queue.enqueue(action);


                                //合体
                                if(chess.mergeTest(chessboard[chess.getPos()]))
                                {
                                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                    {
                                        //插入自己的棋子列表
                                        chess.insertToIndexList(pair);
                                        //记得隐藏其他的棋子
                                        player[pair.playerId].chesslist[pair.chessId].setStatus(Chess.STATUS_HIDING);
                                        action = new Action(pair.playerId,pair.chessId, Action.HIDE);
                                        queue.enqueue(action);
                                    }
                                }
                                //吃掉
                                else if(chess.eatTest(chessboard[chess.getPos()]))
                                {
                                    for(Pair pair:chessboard[chess.getPos()].getIndexlist())
                                    {
                                        //坠落，回到停机坪
                                        player[pair.playerId].chesslist[pair.chessId].setFallen();
                                        action = new Action(pair.playerId,pair.chessId, Action.FALLEN);
                                        queue.enqueue(action);
                                    }
                                }
                            }

                            //刚好到达入口，需要右转
                            if(chess.getPos() == chess.getEntry())
                            {
                                action = new Action(playerid,chessindex, Action.TURNRIGHT);
                                queue.enqueue(action);
                            }

                            player[playerid].chesslist[chessindex] = new Chess(chess);
                            chessboard[chess.getPos()] = new Chess(chess);
                        }
                    }
                    if(dice != 6) turn = (turn + 1)%4;

                    return queue;
                }
            }
            else
            {
                if(dice != 6) turn = (turn + 1)%4;
                return queue;
            }
        }
    }

    //    public void run() {
//
//        Queue<Action> actions;
//        String str ="";
//
//
//        while(!isGameOver())
//        {
//            //如果是联机模型，则向所有玩家发送现在轮到谁了
//            //send_Current_Turn_To_AllPlayer
//
//            //不断扫描是不是有人扔了骰子
//            while (waitDice()) try {
//                UISimulator.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            //不断扫描是不是有人选择了棋子
//            while (waitChoice()) try {
//                UISimulator.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//            //后面的服务器模型时会用到
//    }
    public static void main(String[] args) {

    }
}
