import processing.core.PImage;

import java.util.*;

/*
WorldModel ideally keeps track of the actual size of our grid world and what is in that world
in terms of entities and background elements
 */

final class WorldModel
{
   private EntityFactory entityFactory = new EntityFactory();
   public static final String MAIN_KEY = "collector";
   public static final int MAIN_NUM_PROPERTIES = 4;
   public static final int ID = 1;
   public static final int COL = 2;
   public static final int ROW = 3;

   public static final String OBSTACLE_KEY = "obstacle";
   public static final int OBSTACLE_NUM_PROPERTIES = 4;

   public static final String BASKET_KEY = "atlantis";
   public static final int BASKET_NUM_PROPERTIES = 4;

   public static final String TREE_KEY = "seaGrass";
   public static final int TREE_NUM_PROPERTIES = 5;

   public static final int FISH_REACH = 1;

   public static final String BGND_KEY = "background";
   public static final int BGND_NUM_PROPERTIES = 4;
   public static final int BGND_ID = 1;
   public static final int BGND_COL = 2;
   public static final int BGND_ROW = 3;

   public static final int PROPERTY_KEY = 0;

   private int fruitsCollected = 0;
   private int fruitsOnScreen = 0;


   private int numRows;
   private int numCols;
   private Background background[][];
   private Entity occupancy[][];
   private Set<Entity> entities;

   public WorldModel(int numRows, int numCols, Background defaultBackground)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

   public int getFruitsCollected() {
      return fruitsCollected;
   }

   public int getFruitsOnScreen() {
      return fruitsOnScreen;
   }

   public void setFruitsCollected(int fruitsCollected) {
      this.fruitsCollected = fruitsCollected;
   }

   public void setFruitsOnScreen(int fruitsOnScreen) {
      this.fruitsOnScreen = fruitsOnScreen;
   }

   public int getNumRows(){
      return this.numRows;
   }
   public int getNumCols(){
      return this.numCols;
   }
   public Set<Entity> getEntities(){
      return entities;
   }
   public PImage getCurrentImage(Object entity)
   {
      if (entity instanceof Background)
      {
         return ((Background)entity).getImages()
                 .get(((Background)entity).getImageIndex());
      }
      else if (entity instanceof Entity)
      {
         return ((Entity)entity).getImages().get(((Entity)entity).getImageIndex());
      }
      else
      {
         throw new UnsupportedOperationException(
                 String.format("getCurrentImage not supported for %s",
                         entity));
      }
   }
   public boolean withinBounds(Point pos)
   {
      return pos.y >= 0 && pos.y < this.numRows &&
              pos.x >= 0 && pos.x < this.numCols;
   }
   public boolean isOccupied(Point pos)
   {
      return withinBounds(pos) &&
              getOccupancyCell(pos) != null;
   }
   public Entity getOccupancyCell(Point pos)
   {
      return this.occupancy[pos.y][pos.x];
   }
   public void tryAddEntity(Entity entity)
   {
      if (isOccupied(entity.getPosition()))
      {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
//         throw new IllegalArgumentException("position occupied");
         return;
      }

      addEntity(entity);
   }
   public Optional<Entity> nearestEntity(List<Entity> entities,
                                                Point pos)
   {
      if (entities.isEmpty())
      {
         return Optional.empty();
      }
      else
      {
         Entity nearest = entities.get(0);
         int nearestDistance = distanceSquared(nearest.getPosition(), pos);

         for (Entity other : entities)
         {
            int otherDistance = distanceSquared(other.getPosition(), pos);

            if (otherDistance < nearestDistance)
            {
               nearest = other;
               nearestDistance = otherDistance;
            }
         }

         return Optional.of(nearest);
      }
   }

   public int distanceSquared(Point p1, Point p2)
   {
      int deltaX = p1.x - p2.x;
      int deltaY = p1.y - p2.y;

      return deltaX * deltaX + deltaY * deltaY;
   }

   public Optional<Entity> findNearest(Point pos, Class kind)
   {
      List<Entity> ofType = new LinkedList<>();
      for (Entity entity : this.entities)
      {
         if (entity.getClass().equals(kind))
         {
            ofType.add(entity);
         }
      }

      return nearestEntity(ofType, pos);
   }
   public void addEntity(Entity entity)
   {
      if (withinBounds(entity.getPosition()))
      {
         setOccupancyCell(entity.getPosition(), entity);
         this.entities.add(entity);
      }
   }

   public void moveEntity(Entity entity, Point pos)
   {
      Point oldPos = entity.getPosition();
      if (withinBounds(pos) && !pos.equals(oldPos))
      {
         setOccupancyCell(oldPos, null);
         removeEntityAt(pos);
         setOccupancyCell(pos, entity);
         entity.setPosition(pos);
      }
   }

   public void removeEntity(Entity entity)
   {
      removeEntityAt(entity.getPosition());
   }

   public void removeEntityAt(Point pos)
   {
      if (withinBounds(pos)
              && getOccupancyCell(pos) != null)
      {
         Entity entity = getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
         entity.setPosition(new Point(-1, -1));
         this.entities.remove(entity);
         setOccupancyCell(pos, null);
      }
   }

   public Optional<PImage> getBackgroundImage(Point pos)
   {
      if (withinBounds(pos))
      {
         return Optional.of(getCurrentImage(getBackgroundCell(pos)));
      }
      else
      {
         return Optional.empty();
      }
   }

   public void setBackground(Point pos,
                                    Background background)
   {
      if (withinBounds(pos))
      {
         setBackgroundCell(pos, background);
      }
   }

   public Optional<Entity> getOccupant(Point pos)
   {
      if (isOccupied(pos))
      {
         return Optional.of(getOccupancyCell(pos));
      }
      else
      {
         return Optional.empty();
      }
   }

   public void setOccupancyCell(Point pos,
                                       Entity entity)
   {
      this.occupancy[pos.y][pos.x] = entity;
   }

   public Background getBackgroundCell(Point pos)
   {
      return this.background[pos.y][pos.x];
   }

   public void setBackgroundCell(Point pos,
                                        Background background)
   {
      this.background[pos.y][pos.x] = background;
   }
   public Optional<Point> findOpenAround(Point pos)
   {
      for (int dy = -FISH_REACH; dy <= FISH_REACH; dy++)
      {
         for (int dx = -FISH_REACH; dx <= FISH_REACH; dx++)
         {
            Point newPt = new Point(pos.x + dx, pos.y + dy);
            if (withinBounds(newPt) &&
                    !isOccupied(newPt))
            {
               return Optional.of(newPt);
            }
         }
      }
      return Optional.empty();
   }
   public boolean processLine(String line, WorldModel world,
                              ImageStore imageStore)
   {
      String[] properties = line.split("\\s");
      if (properties.length > 0)
      {
         switch (properties[PROPERTY_KEY])
         {
            case BGND_KEY:
               return parseBackground(properties, world, imageStore);
            case OBSTACLE_KEY:
               return parseObstacle(properties, world, imageStore);
            case BASKET_KEY:
               return parseAtlantis(properties, world, imageStore);
            case TREE_KEY:
               return parseSgrass(properties, world, imageStore);
            case MAIN_KEY:
               return parseMainCollector(properties, world, imageStore);
         }
      }

      return false;
   }
   public boolean parseBackground(String [] properties,
                                         WorldModel world, ImageStore imageStore)
   {
      if (properties.length == BGND_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
                 Integer.parseInt(properties[BGND_ROW]));
         String id = properties[BGND_ID];
         world.setBackground(pt,
                 new Background(id, imageStore.getImageList(id)));
      }

      return properties.length == BGND_NUM_PROPERTIES;
   }

   public boolean parseObstacle(String [] properties, WorldModel world,
                                       ImageStore imageStore)
   {
      if (properties.length == OBSTACLE_NUM_PROPERTIES)
      {
         Point pt = new Point(
                 Integer.parseInt(properties[COL]),
                 Integer.parseInt(properties[ROW]));
         Obstacle entity = (Obstacle)entityFactory.createEntity("OBSTACLE", properties[ID],
                 pt, imageStore.getImageList(OBSTACLE_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == OBSTACLE_NUM_PROPERTIES;
   }


   public boolean parseAtlantis(String [] properties, WorldModel world,
                                       ImageStore imageStore)
   {
      if (properties.length == BASKET_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[COL]),
                 Integer.parseInt(properties[ROW]));
         Basket basket = (Basket)entityFactory.createEntity("BASKET", properties[ID], pt, imageStore.getImageList(BASKET_KEY));
         world.tryAddEntity(basket);
      }
      return properties.length == BASKET_NUM_PROPERTIES;
   }

   public boolean parseSgrass(String [] properties, WorldModel world,
                                     ImageStore imageStore)
   {
      if (properties.length == TREE_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[COL]),
                 Integer.parseInt(properties[ROW]));
         Tree entity = (Tree)entityFactory.createEntity("TREE", properties[ID], pt, imageStore.getImageList(TREE_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == TREE_NUM_PROPERTIES;
   }
   public boolean parseMainCollector(String [] properties, WorldModel world,
                                     ImageStore imageStore)
   {
      if (properties.length == MAIN_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[COL]),
                 Integer.parseInt(properties[ROW]));
         MainCollector entity = (MainCollector)entityFactory.createEntity("MAINCOLLECTOR", properties[ID],
                 pt, imageStore.getImageList(MAIN_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == MAIN_NUM_PROPERTIES;
   }
   public void load(Scanner in, WorldModel world, ImageStore imageStore)
   {
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            if (!processLine(in.nextLine(), world, imageStore))
            {
               System.err.println(String.format("invalid entry on line %d",
                       lineNumber));
            }
         }
         catch (NumberFormatException e)
         {
            System.err.println(String.format("invalid entry on line %d",
                    lineNumber));
         }
         catch (IllegalArgumentException e)
         {
            System.err.println(String.format("issue on line %d: %s",
                    lineNumber, e.getMessage()));
         }
         lineNumber++;
      }
   }



}



