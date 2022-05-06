package com.ltd.lifesearchapp.Detect;

class BreathDetect extends DetectUnit {
    public BreathDetect(long time, RadarDataPool pool) {
        super(false, new BreathImpl(), pool);
        this.time = time;
    }

    interface Interrupter {
        void interrupt();

        void recover();
    }

    private final long time;

    private boolean auto = true;

    private Interrupter interrupter = null;

    public final void setInterrupter(Interrupter interrupter) {
        this.interrupter = interrupter;
    }

    public final void setAuto(boolean auto) {
        this.auto = auto;
    }

    Thread interruptThread = null;

    boolean interrupting = false;

    private void interruptDetect() {
        synchronized (this) {
            if (!interrupting) {
                interrupting = true;
                interruptThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (interrupter != null) {
                            interrupter.interrupt();
                            System.err.println("restart next breath detect");
                            try {
                                interrupter.recover();
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.err.println("interrupt detect finished");
                            synchronized (BreathDetect.this) {
                                interrupting = false;
                            }
                        }
                    }
                });
                interruptThread.start();
            }
        }
    }

    @Override
    protected void onLoopFinished() {
        super.onLoopFinished();
        long et = System.nanoTime();
        long duration = (et - getStartTimeStamp()) / 1000000;//70s
        System.err.println("duration: " + duration);
        if (auto && duration >= time) {
            interruptDetect();
//            System.err.println("ÃΩ≤‚ ±º‰º‰∏Ù£∫"+duration);
        }
    }
}
