iterations = 40
delay = 1

function color(i)
  if i >= 512 then return 0, 0, 0 end

  if i < 256 then return 0,0,i end

  return (i - 256), (i - 256)//2 , (512-i)//3
end
