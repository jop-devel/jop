MAX_ASSOC=5
MAX_FIELDS=5
LOOP_BOUND=10
puts "  static void measure() {"
1.upto(MAX_ASSOC) { |i|    
  args = (0..(i-1)).map { |ix| "objs[#{ix}]" }
  1.upto(MAX_FIELDS) { |j|
    puts "      loop_#{i}_#{j}(#{args.join(",")});"
  }
}
puts "  }"
1.upto(MAX_ASSOC) { |i|
  1.upto(MAX_FIELDS) { |j|
    args = (0..(i-1)).map { |ix| "objs[#{ix}]" }
    formals = (0..(i-1)).map { |ix| "obj#{ix}" }
    decls = formals.map { |var| "Obj #{var}" }

    puts "  static void measure_#{i}_#{j}(#{decls.join(",")}) {"
    puts "    loop_#{i}_#{j}(#{formals.join(",")});"
    puts "  }"

    puts "  static void loop_#{i}_#{j}(#{decls.join(",")}) {"
    puts "    int sum=0;"
    puts "    for(int i = 0; i < #{LOOP_BOUND}; i++) {"
    formals.each do |var|
      fields = (1..j).map { |fix| "f#{fix}" }
      puts "     sum += #{fields.map { |f| "#{var}.#{f}" }.join("+")};"
    end
    puts "    }"
    puts "    out = sum;"
    puts "  }"
  }
}
