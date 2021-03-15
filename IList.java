import javalib.funworld.WorldScene;
import tester.Tester;

interface IList<T> {
  
  // filter a IList 
  IList<T> filter(IPred<T> pred);

  // combine return value so far
  <U> U foldr(IFunc2<T, U, U> func, U base);

  // [x y -> y] y [List-of X] -> Y
  <U> IList<U> map(IFunc<T, U> f);

  // get the total length of the list
  int length();

  // get the first element in the list
  T getFirst();

  // retrieve the rest elements in the list
  IList<T> getRest();
  
  // check if the list passes 
  public boolean ormap(IFunc<T, Boolean> f);

  // append the a list into another list
  IList<T> append(IList<T> list);

}

// an empty list of T
class MtList<T> implements IList<T> {
  
  // filter a IList 
  public IList<T> filter(IPred<T> pred) {
    return this;
  }

  // combine return value so far
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return base;
  }

  // [x y -> y] y [List-of X] -> Y
  public <U> IList<U> map(IFunc<T, U> f) {
    return new MtList<U>();
  }

  // return the length of a list
  public int length() {
    return 0;
  }

  // draw the world
  public WorldScene draw(WorldScene s) {
    return s;
  }

  // get the first element in the list
  public T getFirst() {
    return null;
  }

  // get the rest element in the list
  public IList<T> getRest() {
    return new MtList<T>();
  }
  
  // check if the list passes 
  public boolean ormap(IFunc<T, Boolean> f) {
    return false;
  }

  // append the a list into another list
  public IList<T> append(IList<T> list) {
    return list;
  }
}

// an non-empty list of T
class ConsList<T> implements IList<T> {

  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  /* Template:
   * fields:
   * this.first -- T
   * this.rest -- IList<T>
   * methods:
   * this.filter(IPred<T>, U) -- IList<T>
   * this.foldr(IFunc2<T, U, U, U> -- <U>
   * this.map(IFunc<T, U>) -- <U>
   * this.legnth() -- int
   * this.getFirst() -- T
   * this.getRest() -- IList<T>
   * this.ormap(IFunc<T, U>) -- boolean
   * this.append(IList<T>) -- IList<T>
   * methods for parameters:
   * this.pred.apply(T) -- boolean
   * this.func.apply(T, IList<T>) -- <U>
   * 
   */
  
  // filter a IList 
  public IList<T> filter(IPred<T> pred) {
    if (pred.apply(this.first)) {
      return new ConsList<T>(this.first, this.rest.filter(pred));
    }
    else {
      return this.rest.filter(pred);
    }
  }

  // fold an element into a list of elements
  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return func.apply(this.first, this.rest.foldr(func, base));
  }

  // [x y -> y] y [List-of X] -> Y
  public <U> IList<U> map(IFunc<T, U> f) {
    return new ConsList<U>(f.apply(this.first), this.rest.map(f));
  }

  // return the length of the list
  public int length() {
    return 1 + this.rest.length();
  }

  // get the first element in the list
  public T getFirst() {
    return this.first;
  }

  // get the rest elements in the list
  public IList<T> getRest() {
    return this.rest;
  }
  
  // check if the list passes 
  public boolean ormap(IFunc<T, Boolean> f) {
    return f.apply(this.first) || this.rest.ormap(f);
  }

  // append the element into the list 
  public IList<T> append(IList<T> list) {
    return new ConsList<T>(this.first, this.rest.append(list));
  }
}

// and interface
interface IPred<T> {
  boolean apply(T t);
}

// checks if ship is out of the range
class CheckRangeS implements IPred<Ships> {
  int x;
  int y;

  CheckRangeS(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  /* Template:
   * fields:
   * this.x -- int
   * this.y -- int
   * methods:
   * this.apply(Ships) -- boolean
   */
  
  public boolean apply(Ships t) {
    return t.x >= 0 && t.x <= this.x || t.y >= 0 && t.y <= this.y;
  }
}

// check if bullet is out of the range
class CheckRangeB implements IPred<Bullets> {
  int x;
  int y;

  CheckRangeB(int x, int y) {
    this.x = x;
    this.y = y;
  }
 
  /* Template:
   * fields:
   * this.x -- int
   * this.y -- int
   * methods:
   * this.apply(Bullets) -- boolean
   */
  
  public boolean apply(Bullets t) {
    return t.x >= 0 && t.x <= this.x || t.y >= 0 && t.y <= this.y;
  }
}

//check the collision of the a single buillets with the list of ships
class ExplosionBullets implements IFunc<Bullets, Boolean> {
  IList<Ships> los;

  ExplosionBullets(IList<Ships> los) {
    this.los = los;
  }

  public Boolean apply(Bullets t) {
    return this.los.ormap(new ExplosionChecker2(t));
  }
}

//check the collision of the a single buillet with a single ship
class ExplosionChecker implements IFunc<Bullets, Boolean> {
  Ships ship;

  ExplosionChecker(Ships ship) {
    this.ship = ship;
  }

  public Boolean apply(Bullets t) {
    double x1 = t.x;
    double y1 = t.y;
    double x2 = ship.x;
    double y2 = ship.y;
    double pBullet = Math.hypot(x1, y1);
    double pShips = Math.hypot(x2, y2);
    return Math.abs(pBullet - pShips) <= t.radius + 10;
  }
}

//check the collision of the a single ship with the list of bullets
class ExplosionShips implements IFunc<Ships, Boolean> {
  IList<Bullets> lob;
  //Bullets b;

  ExplosionShips(IList<Bullets> lob) {
    this.lob = lob;
  }

  public Boolean apply(Ships t) {
    return this.lob.ormap(new ExplosionChecker(t));
  }
}

//check the collision of the a single ship with a single bullet
class ExplosionChecker2 implements IFunc<Ships, Boolean> {
  Bullets b;

  ExplosionChecker2(Bullets b) {
    this.b = b;
  }

  public Boolean apply(Ships t) {
    double x1 = t.x;
    double y1 = t.y;
    double x2 = b.x;
    double y2 = b.y;
    double pBullet = Math.hypot(x1, y1);
    double pShips = Math.hypot(x2, y2);
    return Math.abs(pBullet - pShips) <= 10 + b.radius;
  }
}


interface IFunc<A, R> {
  // A -> R
  R apply(A arg);
}

//class for move the bullet
class MoveB implements IFunc<Bullets, Bullets> {
  public Bullets apply(Bullets b) {
    return b.move();
  }
}

//class for move the ship
class MoveS implements IFunc<Ships, Ships> {
  public Ships apply(Ships s) {
    return new Ships(s.x + 1, s.y);
  }
}


//Interface for two-argument function-objects with signature [A1, A2 -> R]
interface IFunc2<A1, A2, R> {
  R apply(A1 arg1, A2 arg2);
}

// class for create initial ship
class InitialShip implements IFunc2<Ships, Integer, IList<Ships>> {
  public IList<Ships> apply(Ships s, Integer count) {
    if (count == 1) {
      return new ConsList<Ships>(s, new MtList<Ships>());
    }
    else {
      return new ConsList<Ships>(s, this.apply(s, count - 1));
    }
  }
}

// draw ships
class DrawShips implements IFunc2<Ships, WorldScene, WorldScene> {
  public WorldScene apply(Ships s, WorldScene ws) {
    return s.draw(ws);
  }
}

// draw bullets
class DrawBullets implements IFunc2<Bullets, WorldScene, WorldScene> {
  public WorldScene apply(Bullets b, WorldScene ws) {
    return b.draw(ws);
  }
}

//class for create the list of new Ships
class LoS implements IFunc2<IList<Ships>, Integer, IList<Ships>> {

  public IList<Ships> apply(IList<Ships> los, Integer n) {
    Ships s = new Ships(0,0).random();
    if (n == 1) {
      return new ConsList<Ships>(s, los);
    }
    else {
      IList<Ships> nlos = new ConsList<Ships>(s, los);
      return new LoS().apply(nlos, n - 1);
    }
  }
}


// check whether it collides bullets
class CheckCollideB implements IFunc2<IList<Bullets>, IList<Bullets>, IList<Bullets>> {
  IList<Ships> list2;

  CheckCollideB(IList<Ships> list2) {
    this.list2 = list2;
  }

  public IList<Bullets> apply(IList<Bullets> list1, IList<Bullets> acc) {
    if (list1.length() == 0) {
      return acc;
    }
    else {
      if (list2.ormap(new ExplosionShips(list1))) {

        return new CheckCollideB(list2).apply(list1.getRest(), acc);
      }
      else {
        return new CheckCollideB(list2).apply(list1.getRest(),
            new ConsList<Bullets>(list1.getFirst(), acc));
      }
    }
  }
}

//class check collise of ship
class CheckCollideS implements IFunc2<IList<Ships>, IList<Ships>, IList<Ships>> {
  IList<Bullets> list2;

  CheckCollideS(IList<Bullets> list2) {
    this.list2 = list2;
  }

  public IList<Ships> apply(IList<Ships> list1, IList<Ships> acc) {
    if (list1.length() == 0) {
      return acc;
    }
    else {
      if (list2.ormap(new ExplosionBullets(list1))) {

        return new CheckCollideS(list2).apply(list1.getRest(), acc);
      }
      else {
        return new CheckCollideS(list2).apply(list1.getRest(),
            new ConsList<Ships>(list1.getFirst(), acc));
      }
    }
  }
}

// check the number of collsion
class CheckNumCol implements IFunc<Integer, Integer> {
  IList<Bullets> lob;
  IList<Ships> los;

  CheckNumCol(IList<Bullets> lob, IList<Ships> los) {
    this.lob = lob;
    this.los = los;
  }

  public Integer apply(Integer n) {
    if (los.ormap(new ExplosionShips(lob))) {

      return n + 1;
    }
    else {
      return n;
    }
  }
}

// calculate the number of destroyed ship
class CheckNum implements IFunc2<IList<Bullets>, IList<Ships>, Integer> {
  int num;

  CheckNum(int num) {
    this.num = num;
  }

  public Integer apply(IList<Bullets> list1, IList<Ships> list2) {
    IList<Bullets> numBulletsHits = new CountHelp(list2).apply(list1, new MtList<Bullets>());
    if (numBulletsHits.length() == 0) {
      return num;
    }
    else {
      return numBulletsHits.length() + num;
    }
  }
}

// countHelper helps count function
class CountHelp implements IFunc2<IList<Bullets>, IList<Bullets>, IList<Bullets>> {
  IList<Ships> list2;

  CountHelp(IList<Ships> list2) {
    this.list2 = list2;
  }

  public IList<Bullets> apply(IList<Bullets> list1, IList<Bullets> acc) {
    if (list1.length() == 0) {
      return acc;
    }
    else {
      if (list2.ormap(new ExplosionShips(list1))) {

        return new CountHelp(list2).apply(list1.getRest(),
            new ConsList<Bullets>(list1.getFirst(), acc));
      }
      else {
        return new CountHelp(list2).apply(list1.getRest(), acc);
      }
    }
  }
}

// update new list of bullets
class Update implements IFunc<IList<Bullets>, IList<Bullets>> {

  IList<Bullets> lob;

  Update(IList<Bullets> lob) {
    this.lob = lob;
  }

  public IList<Bullets> apply(IList<Bullets> base) {
    if (lob.length() == 0) {
      return base;
    }
    else {
      Bullets first = this.lob.getFirst();
      IList<Bullets> rest = this.lob.getRest();
      IList<Bullets> list = new CreateHelp(first, first.gen).apply(first.gen + 1,
          new MtList<Bullets>());
      IList<Bullets> newBase = base.append(list);
      return new Update(rest).apply(newBase);
    }
  }
}

// create bullet 
class CreateHelp implements IFunc2<Integer, IList<Bullets>, IList<Bullets>> {
  Bullets b;
  int count;

  CreateHelp(Bullets b, int count) {
    this.b = b;
    this.count = count;
  }

  public IList<Bullets> apply(Integer id, IList<Bullets> acc) {
    if (id == 0) {
      return acc;
    }
    else {
      return new CreateHelp(this.b, this.count).apply(id - 1,
          new ConsList<Bullets>(b.create(count, id), acc));
    }
  }
}

// examples and tests for IList
class ExamplesIList {
  ExamplesIList() {}
  
  IList<Bullets> lob = new ConsList<Bullets>(new Bullets(),
                         new ConsList<Bullets>(new Bullets(), 
                             new ConsList<Bullets>(new Bullets(), new MtList<Bullets>())));
  
  IList<String> los = new ConsList<String>("apple", 
      new ConsList<String>("banana", new MtList<String>()));
  
  boolean testFilter(Tester t) {
    return t.checkExpect(this.lob.filter(new CheckRangeB(4, 5)), new MtList<Bullets>())
        && t.checkExpect(this.lob.filter(new CheckRangeB(305, -500)), 
            new ConsList<Bullets>(new Bullets(),
                new ConsList<Bullets>(new Bullets(), 
                    new ConsList<Bullets>(new Bullets(), new MtList<Bullets>()))));
  }
  
  //check Method getFirst()
  boolean testgetFirst(Tester t) {
    return t.checkExpect(this.lob.getFirst(), new Bullets())
        && t.checkExpect(this.los.getFirst(), "apple");
  }
  
  //check method getRest()
  boolean testgetRest(Tester t) {
    return t.checkExpect(this.lob.getRest(), new ConsList<Bullets>(new Bullets(), 
                                               new ConsList<Bullets>(new Bullets(), 
                                                   new MtList<Bullets>())))
        && t.checkExpect(this.los.getRest(), new ConsList<String>("banana", new MtList<String>()));
  }
  
  //check length method
  boolean testgetLength(Tester t) {
    return t.checkExpect(this.lob.length(), 3)
        && t.checkExpect(new ConsList<Bullets>(new Bullets(), new MtList<Bullets>()).length(), 1);
  }
  
  //check append method
  boolean testAppend(Tester t) {
    return t.checkExpect(this.los.append(new ConsList<String>("ggg", new ConsList<String>("bbb", 
        new MtList<String>()))), 
        new ConsList<String>("apple", 
            new ConsList<String>("banana", 
                new ConsList<String>("ggg", 
                    new ConsList<String>("bbb", new MtList<String>())))));
  }
}

