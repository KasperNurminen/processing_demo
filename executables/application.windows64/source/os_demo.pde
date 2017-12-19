int particleCount = 500;

float deltaTime;
float time = 0;
PImage img;
Particle[] particles; // holds all particles
// a boolean variable to ensure timescale only changes only
boolean hasSipilaLeft = false;

void setup() {
  fullScreen();  
  randomSeed(1);
  noiseSeed(1);
  particles = new Particle[particleCount];
  float distance = 7; // the distance from which the sipila starts his journey at the start of the demo
  // the first particle shall be a sipila
  particles[0] = new Particle(width * 2 + distance * width, -width * 0.7 - distance * height, -1500);
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

void draw() {  
  /* the draw-method is constantly being run, so everything that needs to be updated constatly is here */

  // small motion blur
  fill(0, 0, 0, 192);
  rect(0, 0, width, height);

  for (int i = 0; i < particleCount; i++) {
    particles[i].drawYourself();
  }

  simulate();
}

void simulate() {
  /* method to handle all simulating of the particles */

  // time slows the first time the sipila appears
  if (particles[0].pos.y < height * 0.1 && !hasSipilaLeft) { // the sipila has not yet arrived
    deltaTime = 0.1;
  } else if (particles[0].pos.y < height * 0.8  && !hasSipilaLeft)  { // the sipila is currently in the view
    deltaTime = 0.02;
  } else { // the sipila has left
    hasSipilaLeft = true;
    deltaTime = 0.07;
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