" Vim syntax file
" Language:    jxl 
" Version:     0.1
" Maintainer:  Nabarun Mondal <nabarun.mondal at gmail.com>
" URL:		    
" Last Change: 2016 Jan 29
" Disclaimer:  It's an absolut basic, very simple and by far not finished
" syntax file! It only recognizes basic keywords and  constructs like comments
" any help is welcome

" Remove any old syntax stuff
syn clear

" syntax highlighting for words that are not identifiers:
" int unit double String Array byte short char long float
syn keyword jxlExternal		import as 
syn keyword jxlConditional	if else where break continue
syn keyword jxlRepeat			while for 
syn keyword jxlType			bool int INT double DEC NUM byte short char long float
syn keyword jxlType			isa type var

syn keyword jxlStatement		return
syn keyword	jxlBoolean		true false
syn keyword jxlConstant		null
syn keyword	jxlTypedef		my me supers  
syn keyword jxlLangClass	    set dict list array select date time instant lfold rfold index rindex sqlmath minmax 
syn keyword jxlLangClass	    system read write json xml matrix thread #atomic atomic random shuffle error bye

" TODO differentiate the keyword class from MyClass.class -> use a match here


syn keyword	jxlOperator		new and or xor not size empty #def def 
syn keyword	jxlOperator		lt le gt ge ne


" same number definition as in java.vim
syn match   jxlNumber		"\<\(0[0-7]*\|0[xX]\x\+\|\d\+\)[lL]\=\>"
syn match   jxlNumber     "\(\<\d\+\.\d*\|\.\d\+\)\([eE][-+]\=\d\+\)\=[fFdD]\="
syn match   jxlNumber     "\<\d\+[eE][-+]\=\d\+[fFdD]\=\>"
syn match   jxlNumber     "\<\d\+\([eE][-+]\=\d\+\)\=[fFdD]\>"

syn match   jxlOperator  "!"
syn match   jxlOperator  "-"
syn match   jxlOperator  "+"
syn match   jxlOperator  "/"
syn match   jxlOperator  "%"
syn match   jxlOperator  "|"
syn match   jxlOperator  "&"
syn match   jxlOperator  "="
syn match   jxlOperator  "*"
syn match   jxlOperator  "<"
syn match   jxlOperator  ">"
syn match   jxlOperator  "@"
syn match   jxlOperator  "\~"
syn match   jxlOperator  "^"
syn match   jxlOperator  "\$"
syn match   jxlOperator  "_"


syn region  jxlString		start=+"+ end=+"+
syn region  jxlString		start=+'+ end=+'+
syn region  jxlString		start=+`+ end=+`+

" Functions
"	def [name] [(prototype)] {
"
syn match   jxlFunction	"\s*\<def\>"

" Comments
syn region jxlComment		start="/\*"	end="\*/"
syn match	jxlLineComment	"//.*"
syn match	jxlLineComment	"\#\#.*"


if !exists("did_jxl_syntax_inits")
    let did_jxl_syntax_inits = 1
    
    " The default methods for highlighting. Can be overridden later
    hi link jxlExternal		Include
    hi link jxlStatement		Statement
    hi link jxlConditional	Conditional
	hi link jxlRepeat			Repeat
    hi link jxlType			Type
    hi link jxlTypedef		Typedef
	hi link	jxlBoolean		Boolean
    hi link jxlFunction		Function
    hi link jxlLangClass		Constant
	hi link	jxlConstant		Constant
	hi link jxlStorageClass 	StorageClass
	hi link jxlOperator		Operator
    hi link jxlNumber			Number
    hi link jxlString			String
	hi link	jxlComment		Comment
	hi link	jxlLineComment	Comment
    hi link jxlSpecial Keyword
    
endif

let b:current_syntax = "jxl"

" if you want to override default methods for highlighting
"hi Conditional	term=bold ctermfg=Cyan guifg=#80a0ff
