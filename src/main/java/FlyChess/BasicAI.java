package FlyChess;

/**
 * Created by gitfan on 3/26/18.
 */
public class BasicAI {

    public int kind,color;
    public static int PEOPLE = 1,PLAYERAI = 2, AUTOAI = 3;//玩家模式，玩家挂机模式（可转换为玩家模式），全自动AI（无法转换为玩家模式）

    public Chess chesslist[];


    //color 和 turn 一一对应
    public BasicAI(int kind, int color){

        if(illegalKind(kind)){
            System.out.println("illegal kind in BasicAI: BasicAI(int kind,int color)");
            System.exit(0);
        }
        else this.kind = kind;

        setTurn(color);
        chesslist = new Chess[4];
        for(int i = 0;i < 4 ; i++){
            chesslist[i] = new Chess(new Pair(color,i),color);
        }
    }

    //设置类型
    private void setTurn(int turn)
    {
        if(turn < 0 || turn >= 4){
            System.out.print("unexpected value in BasicAi,setTurn(int turn)");
            System.exit(0);
        }
        this.color = turn;
    }

    //设置棋子
    public void setChess(int index, Chess chess)
    {
        if(illegalIndex(index)){
            System.out.print("index out of range in BasicAI: setChess(int index,Chess chess)");
            System.exit(0);
        }
        if(chess == null){
            System.out.print("chess in null in BasicAI:setChess(int index,Chess chess)");
            System.exit(0);
        }
        this.chesslist[index] = chess;
    }
    //获得棋子
    public Chess getChess(int index)
    {
        if(illegalIndex(index)){
            System.out.print("index out of range in BasicAI: getChess(int index,Chess chess)");
            System.exit(0);
        }
        return this.chesslist[index];
    }
    //现在轮到我了吗？
    public boolean isMyturn(int turn)
    {
        return (this.color == turn);
    }

    //我的所有棋子都完成了吗？
    public boolean isFinish()
    {
        for(int i = 0; i < 4; i++){
            if(chesslist[i].getStatus() != Chess.STATUS_FINISH) return false;
        }
        return true;
    }

    protected int getKind()
    {
        return this.kind;
    }

    //获取可直接移动的棋子
    protected Queue<Integer> getAvailableMove()
    {
        Queue<Integer> queue = new Queue<>();
        for(int i = 0;i < 4;i++)
        {
            if(chesslist[i].canMove()) queue.enqueue(i);
        }
        return queue;
    }

    //AI自动获取分数最高的棋子选择
    public int ai_choice(int dice, Chess chessboard[])
    {
        Queue<Integer> queue = getAvailableMove();
        if(queue.isEmpty())
        {
            if(dice <= 4) return  -1;
            for (int i = 0; i < 4 ;i++){
                if(chesslist[i].getStatus() == Chess.STATUS_AIRPORT) return i;
            }
        }
        else
        {
            int choice = queue.dequeue();
            int maxval = score(chesslist[choice],dice,chessboard);
            while(!queue.isEmpty()){
                int curr = queue.dequeue();
                int currval = score(chesslist[curr],dice,chessboard);
                if(maxval < currval){
                    choice = curr;
                    maxval = currval;
                }
            }
            if(dice == 5 || dice == 6)
            {
                if(maxval < 1000)
                {
                    for (int i = 0; i < 4 ;i++){
                        if(chesslist[i].getStatus() == Chess.STATUS_AIRPORT) return i;
                    }
                }
            }
            return choice;
        }
        return -1;
    }

    //范围检测
    private static boolean illegalIndex(int idx)
    {
        if(idx < 0 || idx >= 4)
        {
            return true;
        }
        return false;
    }

    private static boolean illegalKind(int kind)
    {
        if(kind != BasicAI.AUTOAI && kind != BasicAI.PLAYERAI && kind != BasicAI.PEOPLE){
            return true;
        }
        return false;
    }

    //选择某个棋子获取的分数
    private static int score(Chess chess, int dice, Chess chessboard[])
    {
        if (chess.sprint())
        {
            if(chess.testGoal(dice)) return 10000;
            else if(54 + chess.getColor()*5 == chess.getPos() && dice != 6) return 500;
            else if(54 + chess.getColor()*5 == chess.getPos() && dice == 6) return 0;
            else return 50;
        }
        else
        {
            if(chess.entry())
            {
                if (dice == 6) return 10000;
                else if(dice == 3) return 4000;
                else return 5000;
            }
            else if(chess.presprint(dice)) return 5000;
            else if(chess.eatTest(chessboard[(chess.getPos() + dice)%52])){
                return (chessboard[(chess.getPos() + dice)%52].getIndexlist().size()+chess.getIndexlist().size())*1500;
            }
            else if(chess.mergeTest(chessboard[(chess.getPos() + dice)%52])){
                return (chessboard[(chess.getPos() + dice)%52].getIndexlist().size()+chess.getIndexlist().size())*1000;
            }
            else
            {
                Chess tmp = new Chess(chess);
                tmp.setPos((chess.getPos() + dice)%52);
                if(tmp.isSuperLucky()) return 3000;
                else if(tmp.isLucky()) return 2000;
                else
                {
                    if(chess.getColor() == Chess.RED) return 2000-(51-chess.getPos()-dice)*40;
                    else if(chess.getColor() == Chess.YELLOW)
                    {
                        if(chess.getPos() + dice >= 14 && chess.getPos() + dice <=51) return (64-chess.getPos()-dice)*40;
                        else return (11 - (chess.getPos()+dice)%52)*40;
                    }
                    else if(chess.getColor() == Chess.BLUE)
                    {
                        if(chess.getPos() + dice >= 27 && chess.getPos() + dice <=51) return (77-chess.getPos()-dice)*40;
                        else return (24 - (chess.getPos()+dice)%52)*40;
                    }
                    else
                    {
                        if(chess.getPos() + dice >= 40 && chess.getPos() + dice <=51) return (90-chess.getPos()-dice)*40;
                        else return (37 - (chess.getPos()+dice)%52)*40;
                    }
                }
            }
        }
    }




}
