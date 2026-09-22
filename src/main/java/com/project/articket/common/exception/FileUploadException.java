package com.project.articket.common.exception;

//java에서 모든 입출력은 예외처리가 필요하니 사용자 정의 예외처리 클래스를 따로 정의한다.

//런타임을 상속받아 예외 클래스를 정의한다
public class FileUploadException extends RuntimeException{

    // 2. [생성자 1] 에러 메시지만 인자로 받아서 예외를 발생시킵니다.
    public FileUploadException(String message){
        super(message); //부모클래스의 생성자를 호출해 온다.
    }

    // 3. [생성자 2] 에러 메시지와 함께 '진짜 원인이 된 예외(Throwable)'까지 통째로 넘겨서 발생시킵니다.
    public FileUploadException(String message, Throwable cause){
        super(message,cause);
    }// 부모 클래스에 메시지와 근본적인 에러 원인을 함께 전달
}
