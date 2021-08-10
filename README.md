# String-Calculator
This calculator is able to interpret and solve string equations. 

# About

This calculator was made for my mod "Hypickle Quiz Bot": https://github.com/vmad18/hypickle-quiz-bot
It is like Wolfram Alpha's calculator however, not as advanced -as Wolfram's- and still may contain some bugs. 
The reason for creating was because there was no available code anywhere else for a string based calculator. 

The code is able to interpret: 
- All trig functions (except for csc, sec, cot, however one could do 1/sin, 1/cos, 1/tan to get the same values) 
- log base 10 and natural log (ln)
- Parentheses 
- Order of operations 
- Mathematical constants: "pi", "phi", and "e"  

# Example
Passing in a string such as "sin(pi/2)" or if in degree mode "sin(90)" would return, of course, 1. 
More complex equations such as "sin(sin(cos(3pi/4))) * ln(e^10) + 72" would return: 65.951 (rounded). 

