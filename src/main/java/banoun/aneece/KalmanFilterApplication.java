package banoun.aneece;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class KalmanFilterApplication {

	public static void main(String[] args) {
		//SpringApplication.run(KalmanFilterApplication.class, args);
		new KalmanFilterChart().framChart();
	}
}
