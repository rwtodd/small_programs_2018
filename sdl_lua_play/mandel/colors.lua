iterations = 100
delay = 50 

local palette = {}
local r, g, b = 0, 256//3, 512//3 
local dr, dg, db = 1, 1, 1
for i = 0,512 do
   palette[i] = { r, g, b }
   r = r + dr
   g = g + dg
   b = b + db
   if r == 255 or r == 0 then dr = dr * -1 end
   if g == 255 or g == 0 then dg = dg * -1 end
   if b == 255 or b == 0 then db = db * -1 end
end

function color(i)
  if i >= 512 then return 0, 0, 0 end
  return table.unpack(palette[i])
end
