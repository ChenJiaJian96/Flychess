package FlyChess;

/**
 * Created by gitfan on 3/28/18.
 */

class Pair
{
    public int playerId;
    public int chessId;
    public Pair(){
        playerId = chessId =-1;
    }
    public Pair(int playerId,int chessId)
    {
        this.playerId = playerId;
        this.chessId = chessId;
    }
    public Pair(Pair that)
    {
        this.playerId = that.playerId;
        this.chessId = that.chessId;
    }
}

public class Chess {

    public static int RED = 0,YELLOW = 1, BLUE = 2,GREEN = 3;

    //5种棋子状态:飞机砰，起飞线，飞行中途,已完成，隐藏状态(多个棋子合体时用到)
    public static int STATUS_AIRPORT = 0,STATUS_STARTLINE = 1 ,STATUS_FLYING = 2,STATUS_FINISH = 3,STATUS_HIDING = 4;

    //1种棋盘的状态:空棋盘
    public static int STATUS_EMPTY = 5;

    //小跳棋（4步）坐标
    public static int luckycolor[];
    //大跳坐标
    public static int supercolor[];
    //中间的棋盘颜色
    public static int colorlist[];

    //每中颜色玩家的棋子对应的起始位置
    public static int originPos[];

    static
    {
        luckycolor = new int[4];
        supercolor = new int[4];

        luckycolor[RED] = 2;
        luckycolor[YELLOW] = 3;
        luckycolor[BLUE] = 0;
        luckycolor[GREEN] = 1;

        supercolor[RED] = 18;
        supercolor[YELLOW] = 31;
        supercolor[BLUE] = 44;
        supercolor[GREEN] = 5;

        colorlist = new int [4];
        colorlist[0] = BLUE;
        colorlist[1] = GREEN;
        colorlist[2] = RED;
        colorlist[3] = YELLOW;

        originPos = new int[4];
        originPos[RED] = 0;
        originPos[YELLOW] = 13;
        originPos[BLUE]  = 26;
        originPos[GREEN] = 39;
    }

    //棋子状态
    private int status;

    //棋子颜色
    private int color;

    //棋子位置
    private int pos;

    // 棋子列表(属于哪个玩家，第几个棋子)
    private Queue<Pair> indexlist;

    public String toString()
    {
        String str="";
        str += "status: " + status +", ";
        str += "color: " + color +", ";
        str += "pos: " + pos +" ";
        str += "\n";
        for(Pair pair:indexlist)
        {
            str = str + "[ " +pair.playerId +" , "+pair.chessId +" ], ";
        }
        return str;
    }

    //是否可以移动
    public boolean canMove()
    {
        return (status == STATUS_STARTLINE || status == STATUS_FLYING);
    }

    //index是chess在棋盘中的位置(属于哪个玩家，第几个棋子)
    //主要是玩家棋子的初始化
    public Chess(Pair index,int color)
    {
        indexlist = new Queue<Pair>();
        indexlist.enqueue(index);
        setColor(color);
        setStatus(STATUS_AIRPORT);
        setPos(originPos[color]);
    }

    //主要是棋盘的初始化
    public Chess(int pos)
    {
        if(52 <= pos && pos <= 56) setColor(RED);
        else if(57 <= pos && pos <= 61) setColor(YELLOW);
        else if(62 <= pos && pos <= 66) setColor(BLUE);
        else if(67 <= pos && pos <= 71) setColor(GREEN);
        else if(0 <= pos && pos <= 51) setColor(colorlist[pos%4]);
        this.status = STATUS_EMPTY;
        this.indexlist = new Queue<Pair>();
        this.pos = pos;
    }

    //复制拷贝用的
    public Chess(Chess that)
    {
        this.status = that.status;
        this.color = that.color;
        this.pos = that.pos;
        Queue<Pair> queue = new Queue<>();
        for(Pair pair:that.getIndexlist())
        {
            queue.enqueue(pair);
        }
        indexlist = queue;
    }

    private void setColor(int color)
    {
        if(illegalColor(color)){
            System.out.print("color out of range in \'setColor\',color: " + color);
            System.exit(0);
        }
        this.color = color;
    }
    public int getColor()
    {
        return color;
    }

    //终点
    public int endPoint()
    {
        if(color == RED) return 57;
        else if(color == YELLOW) return 62;
        else if(color == BLUE) return 67;
        else  return 72;
    }
    //滑行点
    public int getFlyingPoint()
    {
        if(color == RED) return 30;
        else if(color == YELLOW) return 43;
        else if(color == BLUE) return 4;
        else return 17;
    }
    //可以导致跳步后飞步的滑行点
    public int getPreFlyingPoint()
    {
        if(color == RED) return 14;
        else if(color == YELLOW) return 27;
        else if(color == BLUE) return 40;
        else return 1;
    }

    //直接设置棋子在棋盘的位置
    public void setPos(int pos)
    {
        if(illegalPos(pos)) {
            System.out.print("pos out of range in \'setPos\',pos: " + pos);
            System.exit(0);
        }
        this.pos = pos;
    }
    //获取棋子的位置
    public int getPos()
    {
        return pos;
    }

    //根据棋子当前位置确定是否可以连跳
    //警告：不包括那些接近终点的位置
    public boolean isLucky()
    {
        if(pos >= 52) return false;
        return ( (pos%4) == luckycolor[color] );
    }
    //根据棋子当前位置确定是否可以飞跳
    public boolean isSuperLucky()
    {
        return (supercolor[color] == pos);
    }
    //设置3种状态:飞机砰，起飞线，飞行中
    //警告：设置状态时，其它的值是否应该改变？？？
    //还得看棋子的类型！！！！！！
    public void setStatus(int status)
    {
        if(illegalStatus(status)){
            System.out.print("status out of range in \'setStatus\',status: " + status);
            System.exit(0);
        }
        this.status = status;
    }
    public int getStatus()
    {
        return status;
    }
    //设置堕机状态，即回到飞机砰
    //警告：设置状态时，其它的值是否应该改变？？？
    //还得看棋子的类型！！！！！！
    public void setFallen()
    {
        setStatus(STATUS_AIRPORT);
        this.pos = originPos[color];
    }

    //判断两个棋子是否可以合体
    public boolean mergeTest(Chess that)
    {
        if (that.status != STATUS_EMPTY && that.color == this.color && that.pos == this.pos){
            return true;
        }
        return false;
    }
    //判断棋子是否可以吃掉另外一个棋子
    //有一种种情况需要特判！！！
    //飞步穿越中间位置的时候（特判见下面的attack)！！！！！
    public boolean eatTest(Chess that)
    {
        if(that.status != STATUS_EMPTY && that.color != this.color && that.pos == this.pos){
            return true;
        }
        return false;
    }
    //飞步穿越中间位置的时候，是否可以攻击
    public boolean attackTest(Chess that)
    {
        if(color == RED)
        {
            if(that.status != STATUS_EMPTY && that.color == BLUE && that.pos == 64) return true;
            else return false;
        }
        else if(color == YELLOW){
            if(that.status != STATUS_EMPTY && that.color == GREEN && that.pos == 69) return true;
            else return false;
        }
        else if(color == BLUE){
            if(that.status != STATUS_EMPTY && that.color == RED && that.pos == 54) return true;
            else return false;
        }
        else
        {
            if(that.status != STATUS_EMPTY && that.color == YELLOW && that.pos == 59) return true;
            else return false;
        }
    }
    //获取飞步时攻击的中间位置的位置
    public int getAttackPos()
    {
        if(color == RED){
            return 64;
        }
        else if(color == YELLOW){
            return 69;
        }
        else if(color == BLUE)
        {
            return 54;
        }
        else return 59;
    }
    public Queue<Pair> getIndexlist()
    {
        return indexlist;
    }
    public void insertToIndexList(Pair person)
    {
        if(indexlist == null){
            System.out.print("indexlist is null in Chess: insertToIndexList(Pair person)");
            System.exit(0);
        }
        this.indexlist.enqueue(person);
    }
    public void clearIndexList()
    {
        if(indexlist == null){
            System.out.print("indexlist is null in Chess:  clearIndexList()");
            System.exit(0);
        }
        while(!indexlist.isEmpty()) indexlist.dequeue();
    }
    //是否在终点线
    public boolean sprint()
    {
        if(color == RED){
            return (52 <= pos && pos <= 56);
        }
        else if(color == YELLOW){
            return (57 <= pos && pos <= 61);
        }
        else if(color == BLUE){
            return (62 <= pos && pos <= 66);
        }
        else{
            return (67 <= pos && pos <= 71);
        }
    }
    //是否接近终点线
    public boolean presprint(int dice)
    {
        if(color == RED){
            return (pos <= 50 && pos + dice > 50);
        }
        else if(color == YELLOW){
            return (pos <= 11 && pos + dice > 11);
        }
        else if(color == BLUE){
            return (pos <= 24 && pos + dice > 24);
        }
        else{
            return (pos <= 37 && pos + dice > 37);
        }
    }
    //进入终点线
    public void setEndLine(int dice)
    {
        if(color == RED) {
            //是否可以直接到达终点？
            if(pos == 50 && dice == 6){
                setPos(72);
                setStatus(STATUS_FINISH);
            }
            else setPos(dice - 50 + pos + 51);
        }
        else if(color == YELLOW) {
            if(pos == 11 && dice == 6) {
                setPos(72);
                setStatus(STATUS_FINISH);
            }
            else setPos(dice - 11 + pos + 56);
        }
        else if(color == BLUE) {
            if(pos == 24 && dice == 6) {
                setPos(72);
                setStatus(STATUS_FINISH);
            }
            else setPos(dice - 24 + pos + 61);
        }
        else {
            if(pos == 37 && dice == 6) {
                setPos(72);
                setStatus(STATUS_FINISH);
            }
            else setPos(dice - 37 + pos + 66);
        }
    }
    //在终点线反弹
    //有可能直接去到终点
    public boolean rebound(int dice)
    {
        if(color == RED){
            if(pos + dice == 57){
                setPos(72);
                setStatus(STATUS_FINISH);
                return false;
            }
            else if(pos + dice < 57){
                setPos(pos + dice);
                return false;
            }
            else{
                setPos(57 + 57 - dice - pos);
                return true;
            }
        }
        else if(color == YELLOW)
        {
            if(pos + dice == 62){
                setPos(72);
                setStatus(STATUS_FINISH);
                return false;
            }
            else if(pos + dice < 62)
            {
                setPos(pos + dice);
                return false;
            }
            else
            {
                setPos(62 + 62 - dice - pos);
                return true;
            }
        }
        else if(color == BLUE){
            if(pos + dice == 67){
                setPos(72);
                setStatus(STATUS_FINISH);
                return false;
            }
            else if(pos + dice < 67)
            {
                setPos(pos + dice);
                return false;
            }
            else{
                setPos(67 + 67 - dice - pos);
                return true;
            }
        }
        else
        {
            if(pos + dice == 72){
                setPos(72);
                setStatus(STATUS_FINISH);
                return false;
            }
            else if(pos + dice < 72){
                setPos(pos + dice);
                return false;
            }
            else
            {
                setPos(72 + 72 - dice - pos);
                return true;
            }
        }
    }

    //是否在进入终点线的入口
    public boolean entry()
    {
        if(color == RED){
            return (pos == 50);
        }
        else if(color == YELLOW){
            return (pos == 11);
        }
        else if(color == BLUE){
            return (pos == 24);
        }
        else{
            return (pos == 37);
        }
    }

    //进入终点线的入口
    public int getEntry()
    {
        if(color == RED)
        {
            return 50;
        }
        else if(color == YELLOW)
        {
            return 11;
        }
        else if(color == BLUE)
        {
            return 24;
        }
        else return 37;
    }
    //是否到达终点
    public boolean testGoal(int dice)
    {
        if(color == RED){
            return (pos + dice == 57);
        }
        else if(color == YELLOW){
            return (pos + dice == 62);
        }
        else if(color == BLUE){
            return (pos + dice == 67);
        }
        else{
            return (pos + dice == 72);
        }
    }

    /***************************************************************
     *
     * 检查范围的函数
     *
     ****************************************************************/
    private boolean illegalColor(int color)
    {
        if(color < 0 || color >= 4)
        {
            return true;
        }
        return false;
    }
    private boolean illegalStatus(int status)
    {
        if(status != STATUS_AIRPORT && status != STATUS_FLYING && status != STATUS_STARTLINE
                && status != STATUS_EMPTY  && status != STATUS_FINISH && status != STATUS_HIDING)
        {
            return true;
        }
        return false;
    }
    private boolean illegalPos(int pos)
    {
        //73代表已经到达终点
        if(pos < 0 || pos >= 73)
        {
            return true;
        }
        return false;
    }
}