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

== Changes for the allocation analysis
-- The maximum capacity of HashSet was changed in the JDK from 1<<30 to 1<<10, but this did not
   help. So we initialized all HashSets/HashMaps with the right capacity and eliminated resize. 
-- ArrayList.ensureCapacity() has a similar problem, so again we require that the right
   capacity is used when initializing the ArrayList
-- FIXME: Not sure about the right initial capacity in Reducer.reduceCollisionSet
-- Maximal size for voxel map:
   The size of the voxel map is hard to approximate, but here is one attempt:
   Given a line segment intersecting voxels, assume without loss of generality that each of
    (x1-x0),(y1-y0) and (z1-z0) are >= 0. Then when follow the line segment and observe
    voxel _vertices_, at each voxel either x,y or z coordinate increases by VOXEL_SIZE. As 
    each voxel vertex is shared by at most 4 vertices, a safe upper bound should be
      4 * sum{Q \in {X,Y,Z} (MAX_Q-MIN_Q)/VOXEL_SIZE
    should be a safe approximation.
    In my experiments, the estimate was 4*55, and some simple experiments showed at least 79 voxels.
    This shows that a) considering vertices is neccessary, and b) the bound is not too bad.

== Additional changes for the WCET analysis
-- The loops in merge sort were annotated using the fact, that the size of
   the collection is always 2.