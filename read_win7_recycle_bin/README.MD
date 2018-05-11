# Win7 Recycle Bin

I had a case where someone could not get into 
their Windows 7 recycle bin, but I could go in
as the Administrator and see the files in:

    C:\$Recycle.Bin\%SID%

Unfortunatly, the files don't have their original
names and timestamps in this directory.  A 
[great resource][1] on the web explains that the 
`$I....` files are index files with the following
format:

 - Bytes 0-7: $I File header – always set to 01 followed by seven sets of 00.
 - Bytes 8-15: Original file size (little endian)
 - Bytes 16-23: Deleted date/time stamp - seconds since 1601 
 - Bytes 24-543: Original file path/name.

Against all odds, this random Windows 7 machine had 
perl 5.18 on it!  So, I wrote a small script to parse
an `$I` file and give me the info.

I ran it like so:

```bat
for %f in ($I*) do ( perl readi.pl %f >> files.csv )
```

... and pulled the CSV file into a spreadsheet.

[1]: https://dereknewton.com/2010/06/recycle-bin-forensics-in-windows-7-and-vista/

