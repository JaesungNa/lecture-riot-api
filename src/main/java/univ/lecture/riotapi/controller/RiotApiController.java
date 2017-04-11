package univ.lecture.riotapi.controller;

import lombok.extern.log4j.Log4j;
import univ.lecture.riotapi.model.Calculate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import java.util.StringTokenizer;
import java.util.Stack;


/**
 * Created by tchi on 2017. 4. 1..
 */
@RestController
@RequestMapping("/api/v1")
@Log4j
public class RiotApiController {
	@Autowired
	private RestTemplate restTemplate;

	@Value("${riot.api.endpoint}")
	private String riotApiEndpoint;

	@Value("${riot.api.key}")
	private String riotApiKey;

	@RequestMapping(value = "/calc", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	
	public String queryCalculate(@RequestBody String expression) throws UnsupportedEncodingException {
		final String url = riotApiEndpoint;
		String inTime   = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
		int timeInInteger = Integer.parseInt(inTime);
		Calculator calc = new Calculator();
		double result = calc.calculate(expression);
		
		String parsingString = "{\""+expression+"\":{\"teamId\":\"team1\",\"now\":"+timeInInteger+",\"result\":"+result+"}}";
		/*
		 *************** 	test code for developer	***************
		 * 
		 *	String responsePost = "{\"Responded\":"+restTemplate.postForObject(url, parsingString, String.class)+"}";
		 *	Map<String, Object> parsedMap = new JacksonJsonParser().parseMap(parsingString);
		 *
		 */
		
		String responsePost = restTemplate.postForObject(url, parsingString, String.class);
		
		/*
		 *************** 	test code for developer   ***************
		 *	Map<String, Object> parsedMapForMsg = new JacksonJsonParser().parseMap(responsePost);
		 *	Map<String, Object> calculateDetail = (Map<String, Object>) parsedMap.values().toArray()[0];
		 *	Map<String, Object> responseMsg = (Map<String, Object>) parsedMapForMsg.values().toArray()[0];
		 *		
		 * 	String queriedTeamId = (String) calculateDetail.get("teamId");
		 * 	int queriedNow = (int) calculateDetail.get("now");
		 * 	double queriedResult = (Double) calculateDetail.get("result");
		 * 	String queriedMsg = (String) responseMsg.get("msg");
		 * 	Calculate calculate = new Calculate(queriedTeamId, queriedNow, queriedResult);
		 * 	Calculate calculate = new Calculate(queriedTeamId, queriedNow, queriedResult, queriedMsg);
		 *
		 *	return calculate;
		 */
		return responsePost;
	}


	/* 
	 * 학번&이름
	 * 201302392 나재성
	 * 201302391 김환철
	 * 201302493 최기현
	 * 201302487 조민성
	 */
	/**
	 * Created by tchi on 2017. 3. 19..
	 * 
	 * 
	 */
	public class Calculator {
		
		public double calculate(String exp) {
			// 중위식을 후위식으로 변환하는 클래스를 생성해서 얻은 스트링 배열을 가지는 반환값을 매개변수로 가지는 RPN객체를 생성
			String result = new RPN(new Transform().infixToPostfix(expressionTokenizer(exp))).returnResult();

			return Double.parseDouble(result);
		}

		private String[] expressionTokenizer(String exp) {
			String[] stringExpToStringArrayExp;
			int count=0;
			StringTokenizer token = new StringTokenizer(exp, "*/+-()", true);
			stringExpToStringArrayExp = new String[token.countTokens()];
		
	    while(token.hasMoreTokens()){
				String nextToken=token.nextToken();
				stringExpToStringArrayExp[count]=nextToken;
				count++;
			}
			return stringExpToStringArrayExp;
		}
	  
		private class RPN {
			// args에는 입력한 argument가 저장되어 있음.
			Stack<String> stack;

			private RPN(String[] args) {
				stack = new Stack<String>();
				// ArrayStack stack = new ArrayStack(args.length);

				for (int i = 0; i < args.length; i++) {
					String input = args[i];

					// System.out.print(input + " ");

					if (isAnOperator(input)) {
						double y = Double.parseDouble((String) stack.pop());
						double x = Double.parseDouble((String) stack.pop());
						double z = evaluate(x, y, input);
						stack.push("" + z);
					} else
						stack.push(input);
				}

			}

			private String returnResult() {
				return stack.pop();
			}


			private boolean isAnOperator(String s) {
				return (s.length() == 1 && "*/+-".indexOf(s) >= 0);
			}

			private double evaluate(double x, double y, String op) {
				// 연산자별 계산 방식 구분
				double result = 0;

				if (op.equals("+"))
					result = x + y;
				else if (op.equals("-"))
					result = x - y;
				else if (op.equals("*"))
					result = x * y;
				else
					result = x / y;
				return result;
			}
		}

		private class Transform {
			private Stack<String> stack;
			// private ArrayStack stack;

			private String[] infixToPostfix(String[] args) {
				// 중위식을 후위식으로 변환한 결과를 String 배열로 반환하는 코드를 작성하고,
				// 마지막에 변환된 후위식을 출력함.
				stack = new Stack<String>();
				// stack = new ArrayStack(args.length);
				int count = 0;
				int size = 0;

				// *************** 스트링 배열의 크기를 찾기위한 코드 **************************
				for (int i = 0; i < args.length; i++) {
					if ("()".indexOf(args[i]) >= 0) {
						size++;
					}
				}
				size = args.length - size;
				String[] a = new String[size];
				// ***********************************************************
				
				// 후위식으로 변환하기 위한 코드
				for (int i = 0; i < args.length; i++) {
					if (args[i].length() == 1 && "*/+-()".indexOf(args[i]) >= 0) {
						// args[i]의 길이가 1이고, 연산자(A, S, M, D, (, ))에 해당하는 문자가
						// args[i]일 경우
						if (stack.isEmpty()) {
							stack.push(args[i]);// 스택이 비었을땐, push
						} else if (stack.peek().equals("(") || precedence(args[i]) > (precedence((String) stack.peek()))) {
							// 스택안의 연산자가 "("일 경우나 받아온 연산자(args[i])의 우선순위가 스택안의 연산자의
							// 우선순위보다 클 경우, push
							stack.push(args[i]);
						} else {
							while (!(stack.isEmpty()) && (precedence(args[i]) <= (precedence((String) stack.peek())))) {
								// 스택이 비어있지 않고, 받아온 연산자(args[i])의 우선순위가 스택안의 연산자의
								// 우선순위와 같거나 작을 경우 반복문 수행
								if (!(stack.peek().equals("("))) {
									// 스택의 top 값이 "(" 가 아닐 경우만 아래의 코드를 수행
									a[count] = (String) stack.pop();// 받아온 연산자(args[i])의 우선순위가 스택안의 연산자의 우선순위와 같거나 작을 경우 pop
									count++;
								} else {
									stack.pop();// 스택의 top 값이"("일 경우 pop
									break;
								}
							}
							if (precedence(args[i]) != 0) {//
								stack.push(args[i]);
								// pop을 다하고 난 뒤, args[i]의 값이 ")"인 경우를 빼고 받아온 연산자를 스택에 push
							}
						}

					} else {
						// 피연산자는 스트링 배열에 저장
						a[count] = args[i];
						count++;

					}
				}
				while (!(stack.isEmpty())) {
					if (stack.peek() != "(") {
						// 받아올 값이 더이상 없는 경우, 스택이 비어질 때까지 pop해서 스트링 배열에 저장
						a[count] = (String) stack.pop();
						count++;
					}
				}
				return a;
	    }

			private int precedence(String token) {
				// 연산자의 우선 순위를 반환하는 함수 작성
				// 연산자 우선 순위 *,/ > +,- > (,)
				
				if (token.equals("*") || token.equals("/"))
					return 4;
				else if (token.equals("+") || token.equals("-"))
					return 2;
				else if (token.equals("("))
					return 9;
				else if (token.equals(")")) {
					return 0;
				} else
					return 0; // 올바른 값이 안들어 왔을 경우 예외처리
			}
			
		}

	}

}
