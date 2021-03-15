import java.awt.Color;
import java.util.Random;

import javalib.funworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.WorldImage;
import tester.Tester;

interface IGamePieces {
}

// represents a ship
class Ships implements IGamePieces {
  // constants
  int shipRadiusius = 10;
  int shipSpeef = 4;
  Color shipColor = Color.cyan;
  // fields
  int radius;
  int velocity;
  Color color;
  int x;
  int y;
  String dir;

  Ships(int x, int y, String dir) {
    this.radius = shipRadiusius;
    this.velocity = shipSpeef;
    this.color = shipColor;
    this.x = x;
    this.y = y;
    this.dir = dir;
  }
  
  Ships(int x, int y) {
    this.radius = shipRadiusius;
    this.velocity = shipSpeef;
    this.color = shipColor;
    this.x = x;
    this.y = y;
  }
  
  /* Template:
   * fields:
   * this.raidus -- int
   * this.velocity -- int
   * this.color - Color
   * this.x -- int
   * this.y -- int
   * this.dir -- String
   */

  public WorldScene draw(WorldScene ws) {
    WorldImage s = new CircleImage(this.radius, OutlineMode.SOLID, this.color);
    return ws.placeImageXY(s, this.x, this.y);
  }

  // generates a random ship in the world
  public Ships random() {
    int maxHeight = 257;// 300 - 300*(1/7)
    int minHeight = 43;
    int y = new Random().nextInt(maxHeight - minHeight) + minHeight;
    int n = new Random().nextInt(2);
    String s = this.leftRight(n);
    return new Ships(n * 500, y, s);
  }
  
  public Ships random(Random r) {
    int maxHeight = 257;// 300 - 300*(1/7)
    int minHeight = 43;
    int y = r.nextInt(maxHeight - minHeight) + minHeight;
    int n = r.nextInt(2);
    String s = this.leftRight(n);
    return new Ships(n * 500, y, s);
  }

  // determines the direction of ships
  private String leftRight(int n) {
    if (n == 0) {
      // from 0 and moves to the right
      return "right";
    }
    else {
      // from 500  and moves to the left
      return "left";
    }
  }
}

// represents a bullet
class Bullets implements IGamePieces {
  // constants
  int bulletrad = 2;
  int bSpeed = 8;
  Color bColor = Color.PINK;

  // fields
  int radius = bulletrad;
  int velocity = bSpeed;
  Color color = bColor;
  int x;
  int y;
  double angle;
  int gen;
  
  //initial Bullet:
  Bullets() {
    this.x = 250;
    this.y = 300;
  }
  
  //initial Bullet:
  Bullets(int radius, int x, int y, double angle, int gen) {
    this.radius = radius;
    this.x = x;
    this.y = y;
    this.angle = angle;
    this.gen = gen;
  }
  
  // constructor for simple movement with tick change
  Bullets(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // constructor for create/update new bullets after the collision
  Bullets(int radius, int x, int y) {
    this.radius = radius;
    this.x = x;
    this.y = y;
  }

  
  /* Template:
   * fields:
   * this.raidus -- int
   * this.velocity -- int
   * this.color -- color
   * this.x -- int
   * this.y -- int
   * methods:
   * this.draw(WorldScene) -- WorldScene
   * this.move() -- Bullets
   * this.changeAngle(int) -- double
   * this.create(int) -- Bullets
   * this.createHelp(int, IList<Bullets>) -- IList<Bullets>
   */
  
  // draw the bullet as a cirlce image in the world
  public WorldScene draw(WorldScene ws) {
    WorldImage b = new CircleImage(this.radius, OutlineMode.SOLID, bColor);
    return ws.placeImageXY(b, this.x, this.y);
  }
  
  //move the bullet in the world
  public Bullets move() {
    double a = 2 * Math.PI * this.angle;
    double x = (this.x + Math.cos(a)) * bSpeed;
    double y = (this.y + Math.sin(a)) * bSpeed;
    return new Bullets(this.radius, (int) x, (int) y, this.angle, this.gen);
  }
  
  //given the angle in degree
  public double changeangle(int n, int i) {
    // the id of the ball
    return i * (360 / (n + 1));
  }
  
  //create bullets in the world
  public Bullets create(int count, int id) {
    double angle = this.changeangle(count, id);
    int r = this.radius + 2;
    if (r >= 10) {
      return new Bullets(10, this.x, this.y, angle, this.gen + 1);
    }
    else {
      return new Bullets(r, this.x, this.y, angle, this.gen + 1);
    }
  }
}


// examples and tests for IGamePieces
class ExamplesGame {
  ExamplesGame() {}
  
  Ships ship1 = new Ships(10, 20);
  Ships ship2 = new Ships(0, 43);
  
  Bullets bullet1 = new Bullets(100, 200);
  Bullets bullet2 = new Bullets(250, 300);
  
  boolean testRandom(Tester t) {
    return t.checkExpect(this.ship1.random(new Random(1)), new Ships(0, 54, "right"))
        && t.checkExpect(this.ship2.random(new Random(10)), new Ships(0, 112, "right"))
        && t.checkExpect(new Ships(5, 5).random(new Random(5)), new Ships(0, 100, "right"));
  }
  
  boolean testMove(Tester t) {
    return t.checkExpect(this.bullet1.move(), new Bullets(808, 1599))
        && t.checkExpect(this.bullet2.move(), new Bullets(2008, 2400));
  }
  
  boolean testChangeAngle(Tester t) {
    return t.checkInexact(this.bullet1.changeangle(5,6), 60.0, 0.01)
        && t.checkInexact(this.bullet2.changeangle(3,2), 90.0, 0.01);
  }
  
  boolean testCreate(Tester t) {
    return t.checkExpect(this.bullet1.create(20,1), new Bullets(4, 100, 200))
        && t.checkExpect(this.bullet2.create(3,2), new Bullets(4, 250, 300));
  }
  
}
