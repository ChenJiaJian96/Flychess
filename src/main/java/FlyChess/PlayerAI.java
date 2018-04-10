package FlyChess;

/**
 * Created by gitfan on 3/29/18.
 */
public class PlayerAI extends BasicAI {

    public PlayerAI(int color)
    {
        super(BasicAI.PEOPLE,color);
    }

    public PlayerAI(int kind,int color)
    {
        super(kind,color);
    }

    //切换到AI模式
    public void switchToAI()
    {
        setKind(BasicAI.PLAYERAI);
    }

    private void setKind(int kind){
        if(kind != PEOPLE && kind != AUTOAI && kind != PLAYERAI){
            System.out.print("unexpected kind in BasicAi,setKind(int kind)");
            System.exit(0);
        }
        this.kind = kind;
    }

    //切换到玩家模式
    public void switchToUser()
    {
        setKind(BasicAI.PEOPLE);
    }

    public Queue<Integer> available_choice(int dice)
    {
        Queue<Integer> queue = getAvailableMove();
        Queue<Integer> choose = new Queue<Integer>();

        if(queue.isEmpty())
        {
            if(dice <= 4) return  choose;
            for (int i = 0; i < 4 ;i++)
            {
                if(chesslist[i].getStatus() == Chess.STATUS_AIRPORT)
                {
                    choose.enqueue(i);
                }
            }
            return choose;
        }
        else
        {
            while(!queue.isEmpty())
            {
                int val = queue.dequeue();
                choose.enqueue(val);
            }
            if(dice >= 5)
            {
                for (int i = 0; i < 4 ;i++)
                {
                    if(chesslist[i].getStatus() == Chess.STATUS_AIRPORT)
                    {
                        choose.enqueue(i);
                    }
                }
            }
            return choose;
        }
    }

}