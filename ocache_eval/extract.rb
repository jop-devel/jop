# SRAM Data
# No,N2,N4,N8
# L4
# L8
# L16
# L32
assocs = [0,2,4,8]
linesizes = [4,8,16,32]
readlines.join.split(/MODE/).each do |sec|
  data={}
  sec =~ /^ = (\S+)/
  mode = $1
  sec.split("\n").each do |l|
    if(l =~ /Cycles Per Access \[N=\s*(\d+),l=\s*(\d+)\]: ([\d\.]+)/)
      ways = $1.to_i
      linesz = $2.to_i
      cpa = $3.to_f
      (data[ways]||={})[linesz] = cpa
    end
  end  
  next if mode == "field-as-tag"
  next unless data[4]
  if mode !~ /fill-line/
    puts "# #{mode}"
    puts(assocs.map { |w| "N#{w}" }.join(","))
  else
    suffix = "-fill"
  end 
  linesizes.each do |lz|
    fields = assocs.map { |w| data[w][lz] }
    puts((["L#{lz}#{suffix}"] + fields).join(","))
  end
end

#***** ***** MODE = fill-word ***** *****
#Cycles Per Access [N=  8,l= 4]: 1.22