/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import java.lang.Math;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="TeleOp Code", group="Linear Opmode")
public class TeleOp_Code extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();

    private DcMotor wheelLF = null;
    private DcMotor wheelLB = null;
    private DcMotor wheelRF = null;
    private DcMotor wheelRB = null;

    private DcMotor scooperL = null;
    private DcMotor scooperR = null;
    private DcMotor liftL = null;
    private DcMotor liftR = null;

    private Servo clawL = null;
    private Servo clawR = null;

    private void moveRobot(double c_move_LR, double c_move_FB, double c_rotate) {
        double c_move_mag = Math.sqrt(c_move_LR * c_move_LR + c_move_FB * c_move_FB);
        double c_move_ang = 0.0;
        if (c_move_LR == 0.0) {
            if (c_move_FB > 0.0) c_move_ang = Math.PI / 2.0;
            else if (c_move_FB < 0.0) c_move_ang = -Math.PI / 2.0;
        } else if (c_move_LR > 0.0) c_move_ang = Math.atan(c_move_FB / c_move_LR);
        else {
            c_move_ang = Math.atan(c_move_FB / c_move_LR);
            if (c_move_FB >= 0.0) c_move_ang += Math.PI;
            else c_move_ang -= Math.PI;
        }

        double p_wheel_LF_RB = Math.sin(c_move_ang + Math.PI / 4.0) * c_move_mag;
        double p_wheel_LB_RF = Math.sin(c_move_ang - Math.PI / 4.0) * c_move_mag;

        wheelLF.setPower(Range.clip(p_wheel_LF_RB + c_rotate, -1.0, 1.0));
        wheelLB.setPower(Range.clip(p_wheel_LB_RF + c_rotate, -1.0, 1.0));
        wheelRF.setPower(Range.clip(p_wheel_LB_RF - c_rotate, -1.0, 1.0));
        wheelRB.setPower(Range.clip(p_wheel_LF_RB - c_rotate, -1.0, 1.0));
    }

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        wheelLF = hardwareMap.get(DcMotor.class, "wheel_lf");
        wheelLB = hardwareMap.get(DcMotor.class, "wheel_lb");
        wheelRF = hardwareMap.get(DcMotor.class, "wheel_rf");
        wheelRB = hardwareMap.get(DcMotor.class, "wheel_rb");

        scooperL = hardwareMap.get(DcMotor.class, "scooper_l");
        scooperR = hardwareMap.get(DcMotor.class, "scooper_r");
        liftL = hardwareMap.get(DcMotor.class, "lift_l");
        liftR = hardwareMap.get(DcMotor.class, "lift_r");

        clawL = hardwareMap.get(Servo.class, "claw_l");
        clawL = hardwareMap.get(Servo.class, "claw_r");

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        wheelLF.setDirection(DcMotor.Direction.REVERSE);
        wheelLB.setDirection(DcMotor.Direction.REVERSE);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Wheel control
            double c_move_LR = gamepad1.right_stick_x;
            double c_move_FB = -gamepad1.right_stick_y;
            double c_rotate = gamepad1.left_stick_x;

            moveRobot(c_move_LR, c_move_FB, c_rotate);

            // Lift control
            double p_scooper, p_lift;
            p_scooper = Range.clip(gamepad2.left_trigger - gamepad2.right_trigger, -1.0, 1.0);
            p_lift = -gamepad2.left_stick_y;

            scooperL.setPower(p_scooper);
            scooperR.setPower(p_scooper);
            liftL.setPower(p_lift);
            liftR.setPower(p_lift);

            // Claw control
            int c_claw = 0;
            if (gamepad2.left_bumper) c_claw = 1;
            else if (gamepad2.right_bumper) c_claw = -1;

            if (c_claw != 0) {
                clawL.setPosition(clawL.getPosition() + 0.01 * c_claw);
                clawR.setPosition(clawR.getPosition() + 0.01 * c_claw);
            }

            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Wheels", "L(%.2f, %.2f) R(%.2f, %.2f)", wheelLF.getPower(), wheelLB.getPower(), wheelRF.getPower(), wheelRB.getPower());
            telemetry.addData("Lift", "Scooper(%.2f) Lift(%.2f)", p_scooper, p_lift);
            telemetry.addData("Claw Position", "L(%.2f) R(%.2f)", clawL.getPosition(), clawR.getPosition());
            telemetry.update();
        }
    }
}
