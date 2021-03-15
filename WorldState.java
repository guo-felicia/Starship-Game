import tester.Tester;


import java.awt.Color;
import java.util.Random;

import javalib.funworld.World;
import javalib.funworld.WorldScene;
import javalib.worldimages.*;

public class WorldState extends World {

  @Override
  // constant varible
  int wIDTH = 500;
  int hEIGHT = 300;
  int bSPEED = 8;
  int sSPEED = bSPEED / 2;
  int bULLETSRAD = 2;
  Color bCOLOR = Color.PINK;
  int iNCRAD = 2;
  int mAXRAD = 10;
  int sW = 250;
  int sH = 300;
  Bullets iNB = new Bullets(bULLETSRAD, sW, sH, 90.0, 1);
  int cOUNT = new Random().nextInt(2) + 1;

  int ticks;
  int numExp;
  int destShips;
  int blLeft;
  IList<Bullets> lob;
  IList<Ships> los;

  // constructors that only takes in the number of the bullet left
  WorldState(int blLeft) {
    this.ticks = 0;
    this.numExp = 0;
    this.destShips = 0;
    this.blLeft = blLeft;
    this.lob = new MtList<Bullets>();
    this.los = new LoS().apply(new MtList<Ships>(), cOUNT);
  }

  // a complete constructor with all fields
  WorldState(int ticks, int numExp, int destShips, int blLeft, IList<Bullets> lob,
      IList<Ships> los) {
    this.ticks = ticks;
    this.numExp = numExp;
    this.destShips = destShips;
    this.blLeft = blLeft;
    this.lob = lob;
    this.los = los;
  }

  /*
   * Template: fields: this.ticks -- int this.numExp -- int this.destShips -- int
   * this.blLeft -- int this.lob -- IList<Bullets> this.los -- IList<Ships>
   * methods: this.makeScene() -- WorldScene this.onTick() -- WorldState
   * this.moveAll() -- WorldState this.addShips() -- WorldState this.outRange() --
   * WorldState this.collision() -- WorldState this.update() -- WorldState
   * this.onKeyEvent(String) -- WorldState this.worldEnds() -- WorldEnd
   */

  @Override
  public WorldScene makeScene() {
    // CONSTANT
    Color fontColor = Color.BLACK;
    int fontSize = 13;

    // DEFINE VARIABLES
    WorldScene emptyScene = new WorldScene(wIDTH, hEIGHT);
    WorldScene shipScene = this.los.foldr(new DrawShips(), emptyScene);
    WorldScene bulletScene = this.lob.foldr(new DrawBullets(), shipScene);
    String t = "bullets left: " + this.blLeft + "; ships distroyed: " + this.numExp;
    TextImage text = new TextImage(t, fontSize, FontStyle.BOLD, fontColor);
    WorldScene textScene = bulletScene.placeImageXY(text, 150, (hEIGHT - 20));
    return textScene;
  }

  // make the final scene that only shown the ships
  @Override
  public WorldScene makeAFinalScene() {
    // CONSTANT
    Color fontColor = Color.BLACK;
    int fontSize = 13;

    // DEFINE VARIABLES
    WorldScene emptyScene = new WorldScene(wIDTH, hEIGHT);
    WorldScene shipScene = this.los.foldr(new DrawShips(), emptyScene);
    String t = "bullets left: " + this.blLeft + "; ships distroyed: " + this.numExp;
    TextImage text = new TextImage(t, fontSize, FontStyle.BOLD, fontColor);
    WorldScene textScene = shipScene.placeImageXY(text, 150, (hEIGHT - 20));
    return textScene;
  }

  // the method is called every tick of the animation
  @Override
  public WorldState onTick() {
    /*
     * TASKLIST -- onTick StepOne: move the ships and bullets after one tick
     * StepTwo: check is this tick need to add new Ships in StepThree: remove all
     * the ships and bullets that are out of screen StepFour: check whether there is
     * collision occur after the filter,remove the collison ships and bullets
     * StepFive: add new bullets in the lob, the number is determined by how many
     * round this bullets hits(can be calculate by its rad) Angle - used to change
     * the position and radius has to be changed update the number of explosion as
     * well as the destroyed number of the ships
     */
    return this.moveAll().addShips().outRange().collision();
  }

  // move bullets and ships
  @Override
  public WorldState moveAll() {
    // IList<Bullets> lobMove = new MoveB(this.lob).apply(this.numExp);
    return new WorldState(this.ticks + 1, this.numExp, this.destShips, this.blLeft,
        this.lob.map(new MoveB()), this.los.map(new MoveS()));
  }

  // spawn more ships at either the left or right ends of the screen, and then
  // move across the screen.
  // between 1 and 3 inclusive, uniform distribution
  @Override
  public WorldState addShips() {
    if (this.ticks % 28 == 0) {
      return new WorldState(this.ticks, this.numExp, this.destShips, this.blLeft, this.lob,
          new LoS().apply(this.los, cOUNT));
    }
    else {
      return this;
    }
  }

  @Override
  // create a world that filters bullets and ships that out of range
  public WorldState outRange() {
    return new WorldState(this.ticks, this.numExp, this.destShips, this.blLeft,
        this.lob.filter(new CheckRangeB(wIDTH, hEIGHT)),
        this.los.filter(new CheckRangeS(wIDTH, hEIGHT)));
  }

  @Override
  //collision methods
  public WorldState collision() {
    // produce the list of Bullets that dosn't collide with ship
    IList<Bullets> lobBase = new CheckCollideB(this.los).apply(this.lob, new MtList<Bullets>());
    // produce the list of Bullets that collide with ship
    IList<Bullets> newlob = new CountHelp(this.los).apply(this.lob, new MtList<Bullets>());
    // produce the list of Ships that dosn't collide with Bullet
    IList<Ships> newlos = new CheckCollideS(this.lob).apply(this.los, new MtList<Ships>());
    // update the number of collision
    int updatNum1 = new CheckNumCol(this.lob, this.los).apply(this.numExp);
    // update the number of ships that is destroyed
    int updatNum2 = new CheckNumCol(this.lob, this.los).apply(this.destShips);
    // append all new bullets to the old list of bullets only contains the bullet
    // that dosn't collide
    IList<Bullets> addNewBullets = new Update(newlob).apply(lobBase);

    return new WorldState(this.ticks, updatNum1, updatNum2, this.blLeft, addNewBullets, newlos);
  }

  @Override
  // when press "space" produce a bullet,
  // then total bullet left minus 1
  public WorldState onKeyEvent(String key) {
    int num = this.blLeft;
    if (num >= 1) {
      if (key.equals("escape")) {
        return new WorldState(this.ticks, this.numExp, this.destShips, num - 1,
            new ConsList<Bullets>(iNB, this.lob), this.los);
      }
      else {
        return this;
      }
    }
    else {
      return new WorldState(0);
    }
  }

  @Override
  public WorldEnd worldEnds() {
    if (this.blLeft <= 0) {
      return new WorldEnd(true, this.makeAFinalScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

// examples and tests for Games
class ExamplesGames {
  ExamplesGames() {
  }

  // some constant for Bullet and Ship
  int bulletRad = 2;
  Color bCOLOR = Color.PINK;
  int shipRAD = 10;
  int shipSPEED = 4;
  Color sCOLOR = Color.cyan;
  int WIDTH = 500;
  int HEIGHT = 300;
  int BSPEED = 8;
  int SSPEED = BSPEED / 2;
  int BULLETSRAD = 2;
  Color BCOLOR = Color.PINK;
  int INCRAD = 2;
  int MAXRAD = 10;
  int SW = 250;
  int SH = 300;

  // Examples of the Image and Scene for makeScene methods
  Bullets initialBullet = new Bullets(BULLETSRAD, SW, SH, 90.0, 1);
  Bullets b1 = new Bullets(2, 30, 40, 90.0, 1);
  Bullets b2 = new Bullets(4, 30, 40, 180.0, 2);
  Bullets b3 = new Bullets(4, 30, 40, 360.0, 2);
  Ships s1 = new Ships(30, 40);
  Ships s2 = new Ships(200, 350);
  Ships s3 = new Ships(100, 150);
  Bullets bOut = new Bullets(4, 360, 40, 280.0, 2);
  Ships sOut = new Ships(2000, 350);
  // Examples of the List<T>
  IList<Ships> losEM = new MtList<Ships>();
  IList<Ships> los1 = new ConsList<Ships>(s1, losEM);
  IList<Ships> los2 = new ConsList<Ships>(s2, los1);
  IList<Ships> los2U = new ConsList<Ships>(s3, losEM);
  IList<Ships> los3 = new ConsList<Ships>(s3, los2);
  IList<Ships> los3U = new ConsList<Ships>(s2, los2U);
  IList<Bullets> lobEM = new MtList<Bullets>();
  IList<Bullets> lob1 = new ConsList<Bullets>(initialBullet, lobEM);
  IList<Bullets> lob2 = new ConsList<Bullets>(b1, lob1);
  IList<Bullets> lob3 = new ConsList<Bullets>(b2, new ConsList<Bullets>(b3, lobEM));
  IList<Bullets> lobC = new ConsList<Bullets>(b1, lobEM);
  IList<Bullets> lobC1 = new ConsList<Bullets>(b2,
      new ConsList<Bullets>(b2, new ConsList<Bullets>(initialBullet, lobEM)));
  IList<Bullets> lobC2 = new ConsList<Bullets>(b2, new ConsList<Bullets>(b2, lobEM));
  IList<Ships> losOut = new ConsList<Ships>(sOut, los2U);
  IList<Bullets> lobOut = new ConsList<Bullets>(bOut,new MtList<Bullets>());

  WorldImage b = new CircleImage(this.bulletRad, OutlineMode.SOLID, this.bCOLOR);
  WorldImage s = new CircleImage(this.shipRAD, OutlineMode.SOLID, this.sCOLOR);
  WorldScene emptyScene = new WorldScene(500, 300);
  WorldScene bSence = emptyScene.placeImageXY(b, 250, 300);
  WorldScene sSence1 = bSence.placeImageXY(s, 0, 43);

  WorldState myWorld = new WorldState(10);
  WorldState myWorld1 = new WorldState(3, 2, 5, 2, lob1, los1);
  WorldState myWorld2 = new WorldState(11, 2, 5, 6, lob2, los1);
  WorldState myWorld2U = new WorldState(11, 3, 6, 6, lobC1, losEM);
  WorldState myWorld3 = new WorldState(15, 5, 5, 3, lob2, los3);
  WorldState myWorld3U = new WorldState(15, 6, 6, 3, lob1, los3U);
  WorldState myWorld4 = new WorldState(28, 5, 5, 8, lob2, los3);
  WorldState myWorld4U = new WorldState(28, 6, 6, 4, lob1, los3U);
  WorldState ws = new WorldState(1, 0, 0, 0, lobEM, losEM);
  WorldState ws1 = new WorldState(4, 2, 5, 2, lob1, los1);
  WorldState ws2 = new WorldState(12, 2, 5, 6, lob2, los1);
  WorldState ws3 = new WorldState(2, 2, 5, 6, lobOut, losOut);
  WorldState ws4 = new WorldState(2, 2, 5, 6, lobEM, losEM);

  // other type of examples for IList<T>
  IList<Integer> ilon = new ConsList<Integer>(1,
      new ConsList<Integer>(2, new ConsList<Integer>(5, new MtList<Integer>())));
  IList<String> ilos = new ConsList<String>("a", new MtList<String>());
  IList<Boolean> ilob = new MtList<Boolean>();

  // testing draw method
  boolean testImages(Tester t) {
    return t.checkExpect(initialBullet.draw(emptyScene),
        emptyScene.placeImageXY(new CircleImage(2, OutlineMode.SOLID, Color.PINK), 250, 300))
        && t.checkExpect(myWorld.makeScene(), sSence1); // check the random
  }

  // test for moveAll Methods in OnTick
  boolean testMove(Tester t) {
    return t.checkExpect(myWorld.moveAll(),ws)
        && t.checkExpect(myWorld1.moveAll(),ws)
        && t.checkExpect(myWorld2.moveAll(),ws);
  }

  // test for outRange Methods in OnTick
  boolean testOutRange(Tester t) {
    return t.checkExpect(myWorld.outRange(),ws)
        && t.checkExpect(myWorld1.outRange(),ws)
        && t.checkExpect(myWorld4U.outRange(),myWorld4U)
        && t.checkExpect(ws3.outRange(),ws4)
        && t.checkExpect(myWorld3.outRange(),myWorld3);
  }

  // test for addShip Methods in OnTick
  // how to check the random
  boolean testAddShips(Tester t) {
    return t.checkExpect(myWorld.addShips(), myWorld)
        && t.checkExpect(myWorld4.addShips(), myWorld4);
  }

  // testing collison methods Methods in OnTick
  boolean testCollideMain(Tester t) {
    return t.checkExpect(myWorld1.collision(), myWorld1)
        && t.checkExpect(myWorld2.collision(), myWorld2U)
        && t.checkExpect(myWorld3.collision(), myWorld3U);
  }

  // testing collisionHelp method in CollsionS and CollsionB class
  boolean testCollision(Tester t) {
    return t.checkExpect(new CheckCollideB(this.losEM).apply(this.lob1, this.lobEM), this.lob1)
        && t.checkExpect(new CheckCollideB(this.los1).apply(this.lobEM, this.lobEM), this.lobEM)
        && t.checkExpect(lob2.getFirst(), b1)// check!!!
        && t.checkExpect(b1.x, 30)// check!!!
        && t.checkExpect(new CheckCollideB(this.los1).apply(this.lob2, this.lobEM),
            new ConsList<Bullets>(initialBullet, this.lobEM))
        && t.checkExpect(new CheckCollideB(this.los2).apply(this.lob2, this.lobEM), this.lob1)
        && t.checkExpect(new CheckCollideS(this.lobEM).apply(this.los1, this.losEM), this.los1)
        && t.checkExpect(new CheckCollideS(this.lob1).apply(this.losEM, this.losEM), this.losEM)
        && t.checkExpect(new CheckCollideS(this.lob2).apply(this.los3, this.losEM), this.los3U);
  }

  // testing the method in CreateBullets Class and CountHelp Class as well as
  // Update Class
  boolean testCreateBullets(Tester t) {
    return t.checkExpect(new CountHelp(los1).apply(lob2, new MtList<Bullets>()),
        new ConsList<Bullets>(b1, new MtList<Bullets>()))
        && t.checkExpect(new CountHelp(losEM).apply(new MtList<Bullets>(), new MtList<Bullets>()),
            new MtList<Bullets>());
  }

  // testing the method for updating the number of the collides occuring as well
  // as how many ships were destroyed
  boolean testCollidNumber(Tester t) {
    return t.checkExpect(new CheckNum(2).apply(this.lob1, this.losEM), 2)
        && t.checkExpect(new CheckNum(12).apply(this.lobEM, this.losEM), 12)
        && t.checkExpect(new CheckNum(0).apply(this.lob1, this.los2), 0)
        && t.checkExpect(new CheckNum(6).apply(this.lob2, this.los2), 7)
        && t.checkExpect(new CheckNum(7).apply(this.lob2, this.los1), 8)
        && t.checkExpect(new CheckNum(1).apply(this.lob1, this.los3), 1);
  }

  // test for onKeyEvent Methods in WorldState
  boolean testOnKey(Tester t) {
    return t.checkExpect(myWorld.onKeyEvent("s"),myWorld)
        && t.checkExpect(myWorld1.onKeyEvent("escape"),ws);
  }
  

  // check the length method in IList<T>
  boolean testLength(Tester t) {
    return t.checkExpect(ilon.length(), 3) && t.checkExpect(ilos.length(), 1)
        && t.checkExpect(ilob.length(), 0);
  }

  // check ormap function inside the Collison Class
  boolean testOrmap(Tester t) {
    return t.checkExpect(los1.ormap(new ExplosionShips(lob2)), true)
        && t.checkExpect(los1.ormap(new ExplosionShips(lob2)), false)
        && t.checkExpect(losEM.ormap(new ExplosionShips(lob2)), false)
        && t.checkExpect(losEM.ormap(new ExplosionShips(lobEM)), false)
        && t.checkExpect(lobEM.ormap(new ExplosionBullets(los2)), false)
        && t.checkExpect(lob2.ormap(new ExplosionBullets(los2)), true);
  }

  // testing for the class ExplosionShips and class ExplosionBullets
  boolean testExplosionShips(Tester t) {
    return t.checkExpect(new ExplosionChecker2(b1).apply(s2), false)
        && t.checkExpect(new ExplosionChecker2(b1).apply(s1), true)
        && t.checkExpect(new ExplosionChecker(s1).apply(b3), true)
        && t.checkExpect(new ExplosionChecker(s3).apply(b1), false)
        && t.checkExpect(new ExplosionChecker(s1).apply(b1), true);
  }

  // testing the simple methods in the class - ExplosionShips
  boolean testT(Tester t) {
    return t.checkInexact(Math.hypot(b1.x, b1.y), 50.0, 0.01)
        && t.checkInexact(Math.hypot(s1.x, s1.y), 50.0, 0.01) && t.checkExpect(b1.radius, 2)
        && t.checkInexact(Math.hypot(b1.x, b1.y) - Math.hypot(s1.x, s1.y), 0.0, 0.01);
  }

  // test the Update class
  boolean testUpdateILoB(Tester t) {
    return t.checkExpect(new CreateHelp(b1, b1.gen).apply(b1.gen + 1, new MtList<Bullets>()), lob3);
  }

  // the "test", that is the runner of our game.
  boolean testWorld(Tester t) {

    // initializes 10 bullets for the world, we add the first bullet to the list of
    // Bullets in worldState
    WorldState myWorld = new WorldState(10);

    // width the windows to be
    int worldWidth = 500;

    // height the windows to be
    int worldHeight = 300;

    // how fast the tick rate to be
    double tickRate = 1.0 / 28;

    // Start the game with a big bang.
    return myWorld.bigBang(worldWidth, worldHeight, tickRate);
  }
}