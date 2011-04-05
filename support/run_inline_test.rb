# run an inline test specified in e.g., a java file
#
# syntax 1: $test$> command
#   run the command, saving stdout and stderr
# syntax 2: $stdout$> expect
#   compare the string expect to the next line of output in the previously run command
# syntax 3: $grep$> pre-context ^ expect $ post-context
#   compare the string expect to the next line of standard output matching context

def match(ctx, expect, actual)
    actual = actual.strip 
    if(expect != actual) 
      $stderr.puts("! #{ctx} : expected > '#{expect}'")
      $stderr.puts("! #{ctx} : actual   > '#{actual}'")
    else 
      $stderr.puts("| #{ctx} : match > '#{expect}'")
    end
end

file = ARGV[0]

stdin, stdout, stderr = nil,nil,nil
File.readlines(ARGV[0]).each do |l|
  actual,expect="",""
  if l =~ /\$test\$>(.*)$/
    cmd = $1.strip
    $stderr.puts "| executing > '#{cmd}'"
    stdout.close if stdout
    stderr.close if stderr
    if ! system("#{cmd} 2>.err.log >.log")
      $stderr.puts "! system failed > '#{cmd}'"
    end
    stdout = File.open(".log")
    stderr = File.open(".err.log")
  elsif l =~ /\$stdout\$>(.*)$/
    expect = $1.strip
    begin
      actual = stdout.gets
    end while actual && actual.strip == ""
    match("stdout",expect,actual||"")
  elsif l =~ /\$grep\$>(.*?)\^(.*?)(\$.*)?$/
    prectx = $1.strip
    expect = $2.strip
    postctx = ($3||"$")[1..-1].strip
    context = Regexp.new(Regexp.escape(prectx)+"(.*)"+Regexp.escape(postctx))
    while line = stdout.gets
      (actual = $1 ; break) if context =~ line.strip
    end
    match("grep #{prectx} ^$ #{postctx}",expect,actual)
  end
end
