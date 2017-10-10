<?php

if (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['caseRecordId'])) {
  require_once('db_connect.php');

$case_record_id = $_POST['caseRecordId'];
$case_record_status_id = $_POST['caseRecordStatusId'];
$updated_by = $_POST['updatedBy'];
$updated_on = $_POST['updatedOn'];

$SUCCESS_MESSAGE = 'SUCCESS';
$FAILED_MESSAGE = 'FAILED';

if($stmt = $mysqli->prepare("INSERT INTO tbl_case_record_history
  (case_record_id, record_status_id, updated_by, updated_on) VALUES (?,?,?,?)")) {

    $stmt->bind_param('iiis', $case_record_id, $case_record_status_id, $updated_by, $updated_on);

    $stmt->execute();

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
