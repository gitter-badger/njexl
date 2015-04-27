# nJexl
A  language based on apache jexl for software testing needs,
as my experience with software development for over a decade taught me.
A wise man once told me  "Never build a fool proof software. When you complete building one 1.0 version, 
the fool will release foolishness 2.0.". He was 15 years in Industry then, and is an MBA from IIM-A.

Summary : The software testing has become messy, stagnant, with record and playback.
But that is the driving aspect of UI. NONE, and I repeat NONE bothers about the VALIDATION aspect of the software testing.
NJEXL is a small step in that direction.
It is quite easy to understand that declarative/functional programming is easy to be implemented as a testing validation language.  Most of the so called *TEST FRAMEWORKS* gives you asserts. To compare objects, nulls, not nulls. If they are smart, really smart, perhaps list even. Sorry. Real software is not about matching nulls, and equaling objects.

That does not happen.
Real software testing is testing and validating condition and data. In short they are testing what computer theorists call
predicate operations ( predicate logic ).

The formulation is : IS the STATEMENT "S(x)" true for value "x" ?
That is indeed the predicate formulation of testing.

Thus, every test, if expposed in terms of predicate formulation, then the TEST code need not to be tested at all,
under the assumption, the underlying language is tested. The SQL team tests SQL. Users use SQL, and checks that the used SQL is 
indeed correct.

That is indeed the desciption of what is NJEXL. It is a full feldged language with complete focus on software testing.
I am mostly working with the language core part now, but a full fledged Selenium RC kind of extension is available to jump start web testing. The WIKI page showcases syntax and power of NJEXL.

The language is written in Java, and has no resemblence to Java at all. Although it is shipped as an unified JAR, and can be made to run with standard java -jar <jarfile>.jar syntax. Java is too much talk, to less work. Testing should be less talk, more work. Hence, I wrote it so that I can use a language that makes sense to ME and I can use it for daily work. Let me know what you think.
