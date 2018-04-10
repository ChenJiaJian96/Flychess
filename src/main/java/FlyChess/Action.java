package FlyChess;

/**
 * Created by gitfan on 3/26/18.
 */
public class Action
{
    public static int MOVE_TO_STARTLINE = 1;//简单地移动到起始点,用到的参数：playerid,chessid,action

    public static int NORMAL_MOVE = 2;      // 简单地向前走 step 步，不用考虑任何情况,放心走吧，用到的参数：playerid,chessid,action,step

    public static int HIDE = 3;             //隐藏棋子,记得把第playerid个玩家的第chessid个棋子设置为隐藏,用到的参数：playerid,chessid,action

    //击落棋子，回到停机坪,先把第playerid个玩家的第chessid个棋子移动到停机坪，
    //然后记得棋子设置为显示状态，切记记得设置该棋子为显示!!!!
    //用到的参数：playerid,chessid,action
    public static int FALLEN = 4;

    public static int FINISHED = 5;     //棋子到达终点，完成使命，把第playerid个玩家的第chessid个棋子设置为完成状态，用到的参数：playerid,chessid,action

    public static int  REVERSE = 6;     //旋转180度，把第playerid个玩家的第chessid个棋子旋转180度，然后不用做任何事。用到的参数：playerid,chessid,action
    public static int TURNRIGHT = 7;    //右转，把第playerid个玩家的第chessid个棋子向右旋转90度，然后不用做任何事。用到的参数：playerid,chessid,action
    public static int QUICK_MOVE = 8;   //加快速度移动向前移动step步，主要是飞行时或者跳步时用的。用到的参数：playerid,chessid,action,step

    public static int ACTIVATE = 9;     //当前棋子为可选状态

    private int playerid;       //第几个玩家，从0开始数起
    private int chessid;        //玩家的第几个棋子，从0开始数起
    private int action;         //指令类型
    private int step;           //步数

    public Action(int playerid,int chessid,int action)
    {
        this.playerid = playerid;
        this.chessid = chessid;
        this.action = action;
    }
    public Action(int playerid,int chessid,int action,int step)
    {
        this.playerid = playerid;
        this.chessid = chessid;
        this.action = action;
        this.step = step;
    }
    public Action(Action that)
    {
        this.playerid = that.playerid;
        this.chessid = that.chessid;
        this.action = that.action;
        this.step = that.step;
    }
    public Action()
    {
        this.playerid = -1;
        this.chessid = -1;
        this.action = -1;
        this.step = -1;
    }


    public int getPlayerid()
    {
        return playerid;
    }
    public int getChessid()
    {
        return chessid;
    }
    public int getAction()
    {
        return action;
    }
    public int getStep()
    {
        return step;
    }
    /**
     * Returns a string representation of action.
     *
     * @return an action,followed by playerid,chessid,actionkind
     */
    public String toString()
    {
        String ply;
        if(playerid == 0) ply = "Red";
        else if(playerid == 1) ply = "Yellow";
        else if(playerid == 2) ply = "Blue";
        else ply = "Green";

        String str = "[ " + ply +"," + chessid +" ] " ;
        String str1 = step + " steps ";
        if(action == MOVE_TO_STARTLINE)
        {
            return (str + "move_to_startline");
        }
        else if(action == NORMAL_MOVE)
        {
            return (str + "normal_move " + str1);
        }
        else if(action == HIDE){
            return (str + "hide");
        }
        else if(action == FALLEN)
        {
            return (str + "fallen");
        }
        else if(action == FINISHED)
        {
            return (str + "finished");
        }
        else if(action == REVERSE)
        {
            return (str + "revere 180°");
        }
        else if(action == TURNRIGHT)
        {
            return (str + "turnright");
        }
        else return (str + "quick_move " +str1);
    }
    public String toActionString(){
        String str = "";
        str += Integer.toString(this.playerid);
        str += " ";
        str += Integer.toString(this.chessid);
        str += " ";
        str += Integer.toString(this.action);
        str += " ";
        str += Integer.toString(this.step);
        return str;
    }
}