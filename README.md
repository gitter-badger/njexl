# nJexl
A  language based on apache jexl for Business Programming as well as software testing needs.
This is a result of my experience with software development for over a decade.
A wise man once told me  "Never build a fool proof software. When you complete building one 1.0 version, 
the fool will release foolishness 2.0.". He was 15 years in Industry then, and is an MBA from IIM-A.

Summary : The software testing has become messy, stagnant, with record and playback.
But that is the driving aspect of UI. NONE, and I repeat NONE bothers about the VALIDATION aspect of the software testing.
NJEXL is a small step in that direction.
It is quite easy to understand that declarative/functional programming is easy to be implemented as a testing validation language.  Most of the so called *TEST FRAMEWORKS* gives you asserts. To compare objects, nulls, not nulls. If they are smart, really smart, perhaps list even. 

Sorry. Real software is not about matching nulls, and equaling objects.
That does not happen.
Real software testing is testing and validating condition and data. In short they are testing what computer theorists call
predicate operations ( predicate logic ).

The formulation is : IS the STATEMENT "S(x)" true for value "x" ?
That is indeed the predicate formulation of testing.

Thus, every test, if expposed in terms of predicate formulation, then the TEST code need not to be tested at all,
under the assumption, the underlying language is tested. The SQL team tests SQL. Users use SQL, and checks that the used SQL is 
indeed correct.

On the Business Programming front, people are making a mess of what are trivial business processes.
No Business Process is beyond Turing Complete, never will be. Java is a very bad choice at business programming, the verbosity makes it a killer on the loose. Python can do things in 10 lines what Java can do in 100. Scala is a good language and utmost geeky - impractical for business use.   

I rememeber what people told me about XP when I was working on Windows 7. World needs XP on drugs. No fancy stuff, simply XP on drugs. Thus, a langauge, which is a de facto EXCEL on the drugs solves every business need. No, not software development need.
I am not for that. This is to end what become a senseless fad to squeeze moeny out of enterprise customers.
This : http://projects.haykranen.nl/java/

Convolute simple ideas with so much extra layer that the end user is bound to hell : all hopes abandoned ye who entered here.

That is indeed the desciption of what NJEXL is NOT. It is not going to convolute simple ideas into software hell.
It is a full feldged language with complete focus on bsuiness process automation and software testing.

I am mostly working with the language core part now, but a full fledged Selenium RC kind of extension is available to jump start web testing. The WIKI page showcases syntax and power of NJEXL. A language, like ABAP to ensure anyone can write effective business code, with a little bit of training. Yes, connect to DB get Business Data and manipulate them, like you do in excel.
YES. ANYONE can do it. That is the motto. It works.

The language is written in Java, and has no resemblence to Java at all. Although it is shipped as an unified JAR, and can be made to run with standard java -jar <jarfile>.jar syntax. Java is too much talk, to less work. Testing should be less talk, more work. Hence, I wrote it so that I can use a language that makes sense to ME and I can use it for daily work. Let me know what you think. 
