<?php

  if (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['caseRecordId'])) {
    require_once('db_connect.php');

    $case_record_id = $_POST['caseRecordId'];
    $user_id = $_POST['userId'];
    $health_center_id = $_POST['healthCenterId'];
    $complaint = $_POST['complaint'];
    $control_number = $_POST['controlNumber'];
    $case_id = 6;

    $SUCCESS_MESSAGE = 'SUCCESS';
    $FAILED_MESSAGE = 'FAILED';

    if($stmt = $mysqli->prepare("INSERT INTO tbl_case_records
      (case_record_id, case_id, user_id, health_center_id, complaint, control_number)
      VALUES (?,?,?,?,?,?)")) {

        $stmt->bind_param('iiiiss', $case_record_id, $case_id, $user_id, $health_center_id, $complaint, $control_number);

        $stmt->execute();

        // $stmt->bind_result($result);
        //
        // $stmt->fetch();

        $stmt->close();

        echo json_encode(array('result'=>$SUCCESS_MESSAGE));

      } else {

         echo json_encode(array('result' => $FAILED_MESSAGE));

      }

  $mysqli->close();

  } else {

    echo json_encode(array('result' => $FAILED_MESSAGE));

  }

?>
