import processing.core.PImage;

import java.util.List;

public class Atlantis extends Stationary{
    public static final int ATLANTIS_ANIMATION_REPEAT_COUNT = 7;

    public Atlantis(String id, Point position,
                    List<PImage> images, int actionPeriod, int animationPeriod)
    {
        super(id, position,images,actionPeriod,animationPeriod);
    }
//    protected void executeActivity(WorldModel world,
//                                        ImageStore imageStore, EventScheduler scheduler)
//    {
//        scheduler.unscheduleAllEvents(this);
//        world.removeEntity(this);
//    }
    protected void scheduleActions(EventScheduler scheduler,
                                WorldModel world, ImageStore imageStore)
    {
        scheduler.scheduleEvent(this, this.createAnimationAction(ATLANTIS_ANIMATION_REPEAT_COUNT), this.getAnimationPeriod());
    }
}
