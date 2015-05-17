# nJexl

A  language based on apache JEXL for Business Programming as well as software testing needs.
This is a result of my experience and experiment with software development for over a decade.
A wise man once told me  "Never build a fool proof software. When you complete building one 1.0 version, 
the fool will release foolishness 2.0.". He was 15 years in Industry then, and is an MBA from IIM-A.
My team mates in Microsoft judged him the smartest and wisest man in the team. Thus, it is our opinion.
Our personal opinion, not of my company, not of my friends, family or foes. Entirely mine.

## Summary

Software Testing & Automation has become messy, stagnant  trivial *record and playback*.
Yes, anyone should be able to automate, that does not really mean *anyone can automate*.
That actually means : *a great automation engineer can come from anywhere*. 

These so called *automation tools* promise too much, deliver too little. 
These promises are faulty never the less, because the aspect they tackled is the driving aspect of the UI. 
NONE, and I repeat, *NO Tool designed ever bothers about the VALIDATION aspect of the software testing*.

NJEXL is a small step in fixing this. A small step by one random *lone ranger*.
Perhaps this going to be a big step in the direction of *THINK VALIDATION* philosophy in software testing,
which none of the *frameworks*  exhibit now.

## Motivation

Declarative/functional programming style is easy to be implemented as a testing validation philosophy.  
However, the so called *TEST FRAMEWORKS* only give you asserts. 
Those asserts gets used to compare objects, nulls, not nulls. If they designed it real smart, really smart, perhaps 
they would compare list even. That is a matter of some *serious concern*. 
Real software is not about matching nulls, and equaling objects. Yes, 99% of the so called software people believe that.
But no, what you are doing is a real *tiny winy* part of *Unit Tests*.
*Real Testing* does not happen using asserts equaling objects.
Real software testing is validating conditions and data. Testing is not easy. Testing is not trivial.
In short testing is what computer theorists call predicate operations ( predicate logic ).

## Predicate Formulation of Truth

The formulation is : *Is the STATEMENT "S(x)" true for value "x" ?*
That is indeed the predicate formulation of testing.
Thus, every test, if exposed in terms of predicate formulation, and the underlying framework supports 
the predicate formulation, then the TEST code need not to be tested at all!
All you really would be testing is : whether the formula - or predicate is the correct one.
The SQL team tests SQL. Users use SQL, and checks that the used SQL is indeed correct.
Take a look around the expressive power of declarative programming : 


       // for all i > 0 check if l[i] <  l[i-1] --> then the list is NOT sorted
       /*  This is how you do it in nJexl */
       // '_' is the index , '$'' is the implicit loop variable: 
       empty ( select{ _ > 0 and $ < $[_-1] }(l) )  // checks if a list is in sorted order    
       // select select items from a list to a new list, empty() checks if something is size 0 or null  


## Business Process Automation

That begs a bigger question. If the test code can be written in such small pieces of predicate logic,
how the actual code is so much more complex and bigger than the test code? There is something obviously wrong.
The Business Programming front, people are actually making a complete mess of what are actually pretty trivial business processes.

No Business Process is beyond Turing Complete, never will be. So do not preach complexity.
A big industry problem is over dependence on Java, which is a very bad choice at business programming (in fact any programming), 
the verbosity makes it a killer on the loose. Python can do things in 10 lines what Java can do in 100. Scala is a good language and utmost geeky - impractical for business user and use. Python can not talk to Java business objects (Jython does not count, really), 
and then Python is not a business process automation language or a software testing language.
But it is a beautiful language never the less.  
Testing and Business Programming should be less talk, more work. Who tests the test code? 
Who watches the watchers?


## Business IS NOT Technology, definitely NOT JavaScript

I remember what people told me about XP when I was working on Windows 7. 
*World SIMPLY needs XP on drugs*. No fancy stuff, simply XP on drugs. That works.

Business runs in EXCEL. Thus, a language, which is a de-facto EXCEL on the drugs (with data in back-end) solves every business need. 
This is to end what become a senseless fad to squeeze money out of enterprise customers - to sustain the misconception 
that technology is JavaScript and Web and what not. 

I am, personally, unabashedly utterly against the following fad : http://projects.haykranen.nl/java/
Real engineering is about taking complex ideas, and making them simply usable and reusable to customer.
Todays industry does quite the opposite : convolute simple ideas with so much extra layer that the end business is bound to enter the gateway of hell : *all hopes abandoned ye who entered here*. 
They convince users and money payers that what is being accomplished is so complex
that no human but only select few can achieve it. 

## The Truth About Development Speed

Git was developed in 14 days by a single Guy who shall not be named. 
This document is live in git-hub. Ring a bell?

What can be accomplished by 1 person in 7 days, is taking 7 persons 7 months and more. 
That is what the sham of software has become.
The mythical man-month is not a symptom any more, it is a Frankenstein monster now.

That is indeed the description of what NJEXL is NEVER going to be. 
It is not going to convolute simple ideas into software and jargon hell. 
*Inversion of Control* : rubbish.
*Dependency Injection* : more gibberish. Those are not science, those are pseudo-scientific fads.
Science in software is to do Currying. 
Science is how to write less code in any form, to get the job done completely.


## Philosophy

Thus nJexl is a language with it's full focus on *Business Process Automation and Software Testing & Validation*,
not on commercial fads. This has one singular focus in mind : brevity. 
Following is the philosophy of the Language:

* Reduce the number of lines of the code, 
* In every line reduce the number of characters even 
* To boldly go where no developer has gone before - attaining Nirvana in terms of coding
    - get out of the cycle of bugs and fixes


## Something for the Developers 

The WIKI page showcases syntax and power of NJEXL. 
This is a language, like SAP's ABAP to ensure anyone can write effective business code, with a little bit of training. 
Yes, connect to DB get Business Data and manipulate them, like you do in excel. It is better than ABAP.
YES. ANYONE can do it. That is the motto. It should just work. You do not need be *special people* anymore.


Will the need for special peoples go away? No. They are still special, the programmers. They are smart. They are simply misdirected.
They seem to believe that *writing more code makes them superior*. No, the opposite is what great developers think.
Thus, the aim is to give them the push they need : *any code is faulty code!* . Thus, *less code written is less bugs added*.
That is the motto of nJexl. Reduce the coding effort to such a minimum that nothing extra remains.
Make things simple but not simpler.

## Final Words 

The language is (unfortunately) written in Java, and mostly bears no resemblance with Java, some occasional use Java DNA
can be seen when we use imported Java Objects and fields. Why not C/C++? Lack of time. This was entirely written
when at home, holidaying ( no, a real Engineer never goes in a Holiday, 
ask Donald Knuth about [his honeymoon](http://www-cs-faculty.stanford.edu/~uno/cl.html) ), 
and after office at home, 7p.m to 2a.m in the nights one can check the statistics of the check-ins.

I wrote this so that I, personally can use a language that makes sense to ME 
and I can use it for my own daily work - automation validation. Let me know what you think of the style of the language. 
Live long, and Prosper.