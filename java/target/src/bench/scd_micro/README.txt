The code here is taken from the CDx - RTSJ benchmark.
http://adam.lille.inria.fr/soleil/rcd/Download/

Basically, it is the code periodically invoked in the real-time thread to perform collision detection,
see Main.java

Changes:

== Changes to the benchmark
-- The voxel hashing was changed to an iterative version
-- We use a custom merge sort, which is iterative (and quite efficient)
-- We use smaller constants than the original benchmark, due to the limited capacity of
   our target platform (affected classes are Constants and RawFrame)
-- Array.clone() was eliminated from the JDK, as clone() is not properly support on our
   target platform

== Changes in the benchmark/JDK for the allocation analysis
-- bench/JDK: We initialize all HashSets/HashMaps with the right capacity, end forbid calls
   to resize() in the JDK. 
-- bench/JDK: ArrayLists have to be initialized with the final maximal size, and
   ArrayList.ensureCapacity() in the JDK must not be called implicitly.
-- bench/JDK: For the stack, it is mandatory to use Vector.ensureCapacity after Stack.new, 
   again, implicit calls to ensureCapacityHelper in the JDK are forbidden.
-- JDK16: The initial capacity of HashMap has to be a power of 2.
-- JDK16: We always use the given capacity when increasing the ArrayList capacity, and drop the
   requirement that the capacity is always increased by at least minCapacityIncrement.
-- JDK16: The constructor ArrayList(Collection c) uses c.size() as initial capacity, not
   c.size() * 1.1
-- bench: Maximal size for voxel map:
   The size of the voxel map is hard to approximate, but here is a rough one:
   Given a line segment intersecting voxels, assume without loss of generality that each of
   (x1-x0),(y1-y0) and (z1-z0) are >= 0. Then when follow the line segment and observe
   voxel _vertices_, at each voxel vertex either x,y or z coordinate increases by VOXEL_SIZE. As 
   each voxel vertex is shared by at most 4 vertices, a safe upper bound should be
      4 * sum{Q \in {X,Y,Z} (MAX_Q-MIN_Q)/VOXEL_SIZE   
   In my experiments, the estimate was 4*55, and some simple runtimechecks showed 79
   voxels per line segment.
   This shows that a) considering vertices is neccessary, and b) the bound is not too bad.
-- bench: The size of the temporary array in merge sort is annotated (this is unfortunate)

== Additional changes for the WCET analysis
-- The loops in merge sort were annotated using the fact, that the size of
   the collection is always 2.
-- There are many, many unbounded loops left.   
