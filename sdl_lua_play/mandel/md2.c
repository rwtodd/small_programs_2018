#include <SDL.h>
#include<lua.h>
#include<lauxlib.h>
#include<lualib.h>
#include<stdio.h>
  	
#define DIM 600.0
  	
int main() {
       lua_State *lua = luaL_newstate();
       luaL_openlibs(lua);
       luaL_dofile(lua, "colors.lua"); 

       /* fetch the iteration count from the lua file. */
       lua_getglobal(lua, "iterations");
       int iters = (int)lua_tointeger(lua, -1); 
       lua_pop(lua, 1);
       printf("Running %d iterations.\n", iters);

       /* fetch the delay amount from the lua file. */
       lua_getglobal(lua, "delay");
       int delay = (int)lua_tointeger(lua, -1); 
       lua_pop(lua, 1);
       printf("Delaying %d millis per frame.\n", delay);

       /* save a reference to the 'color' function */
       lua_getglobal(lua, "color");
       int color_ref = luaL_ref(lua, LUA_REGISTRYINDEX);

       /* setup SDL */
       if(SDL_Init(SDL_INIT_VIDEO) != 0) {
           printf("SDL_Init failed: %s\n", SDL_GetError());
           return -1;
       }

       SDL_Window *window = SDL_CreateWindow("HI", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, DIM, DIM, SDL_WINDOW_SHOWN);
       SDL_Surface *surface = SDL_GetWindowSurface(window);
  		
       double fact = 2;
       double cx = -0.74364500005891;
       double cy = 0.13182700000109;
  		
       while (iters--)  {
            double xa = cx - fact;
            double ya = cy - fact;
            int y;
  			
            for (y = 0; y < DIM; y++) {
                 Uint8 *pixline = surface->pixels + y*surface->pitch;
                 double y0 = ya + y/DIM*2*fact;
                 int x;
                 for (x = 0; x < DIM; x++) {
                      double x0 = xa + x/DIM*2*fact;
                      double xn = 0, yn = 0, tmpxn;
                      int i;
                      for (i = 0; i<512; i++) {
                           tmpxn = xn*xn - yn*yn + x0;
                           yn = 2*xn*yn + y0;
                           xn = tmpxn;
                           if (xn*xn + yn*yn > 4)
                                break;  // approximate infinity
                      }
                      lua_rawgeti(lua, LUA_REGISTRYINDEX, color_ref);
                      /* lua_getglobal(lua, "color"); */
                      lua_pushnumber(lua, i);
                      lua_call(lua, 1, 3);
                      pixline[x*4] = (char)lua_tointeger(lua, -1);   /* B */
                      pixline[x*4+1] = (char)lua_tointeger(lua, -2); /* G */
                      pixline[x*4+2] = (char)lua_tointeger(lua, -3); /* R */
                      pixline[x*4+3] = 255;
                      lua_pop(lua, 3);
                 }
            }
  			
            SDL_UpdateWindowSurface(window);
            SDL_Delay(delay);
            fact *= 0.80;
       }
  		
       SDL_DestroyWindow(window);
       SDL_Quit();
       return(0);
  }

