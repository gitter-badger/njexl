" Vim syntax file
" Language: New Java Expression Language
" Maintainer: Nabarun Mondal
" Latest Revision: 28 Jan 2015
"
"
if exists("b:current_syntax")
    finish
endif


" Comments
syn match  njexlComment "\v//.*$"
syn match  njexlComment "\v##.*$"
syn region njexlComment start="/\*" skip="\v\\." end="\*/"


" Instructions 
syn keyword njexlKeywords nop eval goto import export exists call return  
syn keyword njexlKeywords null as empty or and defined int INT long bool date time double NUM DEC size 
syn keyword njexlKeywords if else else for while index lfold rfold time date def #def #atomic 
syn keyword njexlKeywords set list dict and or ne eq gt ge lt le
syn keyword njexlKeywords atomic str thread system json read write xml matrix 
syn keyword njexlKeywords random shuffle range sqlmath minmax break continue

syn match   njexlSpecial      "[_A-Z]*_"

syn keyword njexlBoolean true false 
syn match   njexlInstruction  "[a-zA-Z]\+:[a-z_A-Z]\+"


" Strings
syn region njexlString start=/\v"/ skip=/\v\\./ end=/\v"/
syn region njexlString start=/\v'/ skip=/\v\\./ end=/\v'/
syn region njexlString start=/\v`/ skip=/\v\\./ end=/\v`/


" Variables
syn match njexlVariable "\$[a-zA-Z_0-9]*"
syn match njexlVariable "@[_0-9]\+"


" Constants
syn match njexlInteger "[0-9]\+"
syn match njexlNumber  "[+-][0-9]\+\.[0-9]\+"


" Label
syn match  njexlReference "\v\{[a-zA-Z][a-z0-9_A-Z]*\}"
syn match  njexlLabelDeclare "\*\* [a-zA-Z][a-zA-Z_0-9]*"


syn match njexlOperator  "!"
syn match njexlOperator  "-"
syn match njexlOperator  "+"
syn match njexlOperator  "/"
syn match njexlOperator  "%"
syn match njexlOperator  "|"
syn match njexlOperator  "&"
syn match njexlOperator  "="
syn match njexlOperator  "*"
syn match njexlOperator  "<"
syn match njexlOperator  ">"
syn match njexlOperator  "@"
syn match njexlOperator  "~"
syn match njexlOperator  "^"



" Highlight
hi link njexlLabelDeclare Keyword 
hi link njexlLabelReference Label 
hi link njexlComment Comment
hi link njexlKeywords Keyword
hi link njexlSpecial Keyword
hi link njexlInstruction Function 
hi link njexlBoolean Boolean
hi link njexlString String
hi link njexlInteger Constant
hi link njexlNumber Constant
hi link njexlVariable Identifier
hi link njexlOperator Operator

let b:current_syntax = "jxl"
