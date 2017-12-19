import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class os_demo extends PApplet {

int particleCount = 500;

float deltaTime;
float time = 0;
PImage img;
Particle[] particles; // holds all particles
// a boolean variable to ensure timescale only changes only
boolean hasSipilaLeft = false;

public void setup() {
    
  randomSeed(1);
  noiseSeed(1);
  particles = new Particle[particleCount];
  float distance = 7; // the distance from which the sipila starts his journey at the start of the demo
  // the first particle shall be a sipila
  particles[0] = new Particle(width * 2 + distance * width, -width * 0.7f - distance * height, -1500);
  for (int i = 1; i < particleCount; i++) {
    float x = random(0, width);
    float y = random(0, height);
    float mass = sq(random(1, 3));
    particles[i] = new Particle(x, y, mass);
  }
  // set the sipila's starting speed
  particles[0].vel.x = -400;
  particles[0].vel.y = 400*height/width;

  background(0);
  img = loadImage("sipila.png");
}

public void draw() {  
  /* the draw-method is constantly being run, so everything that needs to be updated constatly is here */

  // small motion blur
  fill(0, 0, 0, 192);
  rect(0, 0, width, height);

  for (int i = 0; i < particleCount; i++) {
    particles[i].drawYourself();
  }

  simulate();
}

public void simulate() {
  /* method to handle all simulating of the particles */

  // time slows the first time the sipila appears
  if (particles[0].pos.y < height * 0.1f && !hasSipilaLeft) { // the sipila has not yet arrived
    deltaTime = 0.1f;
  } else if (particles[0].pos.y < height * 0.8f  && !hasSipilaLeft)  { // the sipila is currently in the view
    deltaTime = 0.02f;
  } else { // the sipila has left
    hasSipilaLeft = true;
    deltaTime = 0.07f;
  }

  time += deltaTime;

  // for all particles...
  for (int i = 1; i < particleCount; i++) {
    // apply forces originating from all other particles
    for (int j = 0; j < particleCount; j++) {
      if (j == i) continue; // don't apply force from itself
      Force force = particles[i].forceCausedByAnothedParticle(particles[j]);
      particles[i].accelerate(force, deltaTime);
    }
    // all particles also are constantly slowing down if no other force is being applied
    // i.e this simulates friction 
    particles[i].slowDown();
  }

  // finally update the positions
  for (int i = 0; i < particleCount; i++) {
    particles[i].move(deltaTime);
  }
}
class Force {
  float x, y;

  Force(float xForce, float yForce) {
    x = xForce;
    y = yForce;
  }

  public void addForce(Force another) {
    /* Adds two forces */
    x += another.x;
    y += another.y;
  }

  public Force multiply(float scale) {
    /* multiplies the x- and y-forces by a float */
    return new Force(x * scale, y * scale);
  }
}
float forceMultiplier = 10000;
/* the minimum distance that the particle forces influence each other
/* this is needed because otherwise particles that are very close to each other would have insane 
/* amounts of energy */
float minDistance = 10; 
float maxForce = 100000; // upper cap to possible force amount
float particleDistance = 50; // distance where pull force transforms into push force


class Particle {
  PVector pos, vel;
  float brightness, colorB, colorC, mass;
  boolean isBigParticle;
  Particle(float x, float y, float particle_mass) {
    pos = new PVector(x, y);
    vel = new PVector(0, 0);
    brightness = random(0.4f, 1);
    mass = particle_mass; 

    /* color stuff, creating a color scheme */
    float a = random(-1, 4);
    colorB = exp(a);
    colorC = exp(a/2);

    if (abs(mass) > 20) {
      isBigParticle = true;
    } else {
      isBigParticle = false;
    }
  }

  public void drawYourself() {
    noStroke();
    /* creating a color scheme */
    fill(map(brightness, 0, 1, 0, 255), 
      map(pow(brightness, colorB), 0, 1, 0, 255), 
      map(pow(brightness, colorC), 0, 1, 32, 160));

    if (isBigParticle) { // the sipila
      image(img, this.pos.x-drawDiameter()/2, this.pos.y-drawDiameter()/2, drawDiameter(), drawDiameter());
    } else { // other particles
      ellipse(this.pos.x, this.pos.y, drawDiameter(), drawDiameter());
    }
  }

  public float drawDiameter() {
    /* diameter = absolute value of the mass squared times 10 */
    return sqrt(abs(mass)) * 10;
  }

  public void accelerate(Force force, float deltaTime) {
    /* calculate the current velocity */
    float multiplier = deltaTime / sqrt(mass) / 3;
    vel.x += force.x * multiplier;
    vel.y += force.y * multiplier;
  }

  public void move(float deltaTime) {
    /* calculate the current position */
    pos.x += vel.x * deltaTime;
    pos.y += vel.y * deltaTime;

    // warp around the screen edges
    if (!isBigParticle) { // the sipila warps differently
      pos.x = (pos.x % width + width) % width;
      pos.y = (pos.y % height + height) % height;
    } else {
      if (pos.x > width*2) {
        vel.x = -abs(vel.x);
      }
      if (pos.x < -width) {
        vel.x = abs(vel.y);
      }
      if (pos.y > height*2) {
        vel.y = -abs(vel.y);
      }
      if (pos.y < -width) {
        vel.y = abs(vel.y);
      }
    }
  }

  public void slowDown() {
    /* simulation of friction */
    vel.x *= 1 - 1/(1+mass*10);
    vel.y *= 1 - 1/(1+mass*10);
  }

  public Force forceCausedByAnothedParticle(Particle another)  {
    /* calculations for the forces */
    float distance = dist(this.pos.x, this.pos.y, another.pos.x, another.pos.y);
    distance = max(distance, minDistance);

    // calculate the unit vectors
    float differenceNormalizedX = (another.pos.x - this.pos.x) / distance;
    float differenceNormalizedY = (another.pos.y - this.pos.y) / distance;
    Force forceDirection = new Force(differenceNormalizedX, differenceNormalizedY);

    float force = 0;
    // calculate the scalar part of the force 
    if (!another.isBigParticle) {
      force += forceMultiplier / pow(distance, 2);
      force -= forceMultiplier * particleDistance / pow(distance, 3);
    } else {
      // the sipila has a different kind of force
      distance -= another.drawDiameter() / 2 * 0.85f;
      force += 3 * forceMultiplier / pow(distance, 2);
    }
    // the bigger the particle, the more force it has
    force *= another.mass;
    // ensure that the force is within the limits
    force = min(force, maxForce);
    force = max(force, -maxForce);

    // combine the unit vector with the scalar force
    return forceDirection.multiply(force);
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "os_demo" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
