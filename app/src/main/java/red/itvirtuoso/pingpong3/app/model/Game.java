package red.itvirtuoso.pingpong3.app.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kenji on 15/03/12.
 */
public abstract class Game {
    private static final int DEFAULT_BALL_SPEED = 500;
    private static final int FRAME_RATE = 60;

    private int mBallSpeed;
    private int mWaitTime;
    private int mTolerance;

    private Set<GameAction> mListeners = new HashSet<>();
    private ExecutorService mService;
    private List<Reserve> mReserves = new ArrayList<>();

    private class Reserve {
        private long mTime;
        private GameEvent mEvent;

        private Reserve(long delayTime, GameEvent event) {
            mTime = delayTime + System.currentTimeMillis();
            mEvent = event;
        }
    }

    public Game() {
        this(DEFAULT_BALL_SPEED);
    }

    public Game(int ballSpeed) {
        mBallSpeed = ballSpeed;
        mWaitTime = 1000 / FRAME_RATE;
        mTolerance = mWaitTime * 2;

        mService = Executors.newSingleThreadExecutor();
        mService.execute(new Loop());
        mService.shutdown();
    }

    public long getTolerance() {
        return mTolerance;
    }

    public void shutdown() {
        mService.shutdownNow();
    }

    public void swing(PlayerType type) {
        synchronized (mReserves) {
            mReserves.add(new Reserve(0, GameEvent.SERVE));
            mReserves.add(new Reserve(mBallSpeed * 1, GameEvent.FIRST_BOUND));
            mReserves.add(new Reserve(mBallSpeed * 2, GameEvent.SECOND_BOUND));
        }
    }

    public void addListener(GameAction listener) {
        mListeners.add(listener);
    }

    public void removeListener(GameAction listener) {
        mListeners.remove(listener);
    }

    private class Loop implements Runnable {

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            while (!Thread.interrupted()) {
                sendEvent();
                now += mWaitTime;
                waitNextFrame(now);
            }
        }

        private void sendEvent() {
            long now = System.currentTimeMillis();
            ArrayList<Reserve> actionedList = new ArrayList<>();
            synchronized (mReserves) {
                for (Reserve reserve : mReserves) {
                    if (now < reserve.mTime) {
                        continue;
                    }
                    for (GameAction listener : mListeners) {
                        listener.onGameAction(reserve.mEvent);
                    }
                    actionedList.add(reserve);
                }
                mReserves.removeAll(actionedList);
            }
        }

        private void waitNextFrame(long nextTime) {
            long elapsed = nextTime - System.currentTimeMillis();
            if (elapsed <= 0) {
                return;
            }
            try {
                Thread.sleep(elapsed);
            } catch (InterruptedException e) {
            /* nop */
            }
            return;
        }
    }
}
