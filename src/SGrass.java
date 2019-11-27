import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SGrass extends ActiveEntity{
    public static final String FISH_KEY = "fish";
    public static final String FISH_ID_PREFIX = "fish -- ";
    public static final int FISH_CORRUPT_MIN = 20000;
    public static final int FISH_CORRUPT_MAX = 30000;


    public SGrass(String id, Point position,
                List<PImage> images, int actionPeriod)
    {
        super(id, position, images, actionPeriod);
    }

    protected void executeActivity(WorldModel world,
                                      ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Point> openPt = world.findOpenAround(getPosition());

        if (openPt.isPresent())
        {
            Fish fish = getPosition().createFish(FISH_ID_PREFIX + getId(),
                    openPt.get(), FISH_CORRUPT_MIN +
                            Functions.rand.nextInt(FISH_CORRUPT_MAX - FISH_CORRUPT_MIN),
                    imageStore.getImageList(FISH_KEY));
            world.addEntity(fish);
            fish.scheduleActions(scheduler, world, imageStore);
        }

        scheduler.scheduleEvent(this,
                this.createActivityAction(world, imageStore),
                getActionPeriod());
    }

}
