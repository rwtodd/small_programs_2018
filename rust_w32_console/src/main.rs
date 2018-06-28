// Just messing around with Rust FFI... 
// a little program that uses Kernel32.dll to spray the
// screen with random chars.
extern crate rand;

use rand::prelude::*;
use std::ffi::CString;
use std::io::{self,Write,Result};

#[derive(Default)]
#[repr(C)]
struct Coord {
  x: i16,
  y: i16,
}

#[derive(Default)]
#[repr(C)]
struct SmallRect {
  left: i16,
  top: i16,
  right: i16,
  bottom: i16,
}

#[derive(Default)]
#[repr(C)]
struct BufferInfo {
  size : Coord,
  cursor_pos : Coord,
  attributes : u16,
  window : SmallRect,
  max_size: Coord, 
}

type HANDLE = *const i8;
#[link(name = "kernel32")]
extern {
   #[link_name = "GetStdHandle"]
   fn get_std_handle(which: i32) -> HANDLE;

   #[link_name = "SetConsoleTitleA"]
   fn set_console_title(title: *const i8) -> i32; 
 
   #[link_name = "GetConsoleScreenBufferInfo"]
   fn get_console_screenbuffer_info(h: HANDLE, bi: *mut BufferInfo) -> i32;

   #[link_name = "SetConsoleCursorPosition"]
   fn set_console_curpos(h: HANDLE, pos: Coord) -> i32;
}

fn lookup_screen_size(h: HANDLE) -> SmallRect {
   let mut bi : BufferInfo = Default::default();
   unsafe { get_console_screenbuffer_info(h, &mut bi) };
   bi.window 
}  

fn main() -> Result<()> {
    let outh = unsafe { get_std_handle(-11) }; 
    println!("Hello, world!: {:?}", outh);
  
    let cstr_title = CString::new("Goofy Chars!").unwrap();
    unsafe { set_console_title(cstr_title.as_ptr()) };

    // get ready to fill the screen
    let win = lookup_screen_size(outh);
    let mut rng = thread_rng();
    let stdout = io::stdout();
    let mut stdout = stdout.lock(); 

    for _ in 0..=(win.bottom-win.top)*(win.right-win.left)*2 {
        let loc = Coord { x: rng.gen_range(win.left,win.right+1),
                          y: rng.gen_range(win.top,win.bottom+1) };
        unsafe { set_console_curpos(outh, loc); }
        let chr = rng.gen_range::<u8>(33,127) as char;
        write!(stdout, "{}",chr)?;
        stdout.flush()?;
    }

    Ok(())
}
