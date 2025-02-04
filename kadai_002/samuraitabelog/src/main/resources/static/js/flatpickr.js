let maxDate = new Date();
maxDate = maxDate.setMonth(maxDate.getMonth() + 3);

// 予約日用設定
flatpickr("#bookingDatePicker", {
//	mode: "range",
	locale: 'ja',
	minDate: 'today',
	disable: [
        function(date) {
            // 曜日 (0 = 日曜日, 1 = 月曜日, ..., 6 = 土曜日) が含まれているかチェック
            return disabledDays.includes(date.getDay());
        }
    ],
	maxDate: maxDate
});

// 予約時刻用設定
  flatpickr("#reservationTime", {
    enableTime: true,  // 時間選択を有効化
    noCalendar: true,  // 日付を非表示
    dateFormat: "H:i", // 表示フォーマット (24時間形式)
    time_24hr: true,  // 24時間表記
    minuteIncrement: 60, // 1時間単位で選択
    disable: [
      function (date) {
        const selectedDate = document.querySelector("#reservationDate").value;
        const crossDay = document.querySelector("#isCrossDay").checked;

        // 営業終了時刻が開始時刻より早い (日をまたぐ場合)
        if (crossDay || storeHours.start > storeHours.end) {
          const currentDate = new Date(selectedDate + "T" + date.toTimeString());
          const startTime = new Date(selectedDate + "T" + storeHours.start);
          const endTime = new Date(selectedDate + "T" + storeHours.end);
          endTime.setDate(endTime.getDate() + 1); // 終了時刻が翌日の場合

          return currentDate < startTime || currentDate > endTime;
        }

        // 通常の営業時間
        return date < new Date("1970-01-01T" + storeHours.start) || 
               date > new Date("1970-01-01T" + storeHours.end);
      },
    ],
  });

// 店舗登録・編集用設定
flatpickr("#timePicker", {
    enableTime: true, // 時間選択を有効化
    noCalendar: true, // 日付を非表示
    dateFormat: "H:i", // 表示フォーマット (24時間形式)
    time_24hr: true, // 24時間表記
    minuteIncrement: 60, // 1時間単位で選択
    defaultHour: 0, // デフォルトの開始時間
    minTime: "00:00", // 開始時間の制限
    maxTime: "23:00", // 終了時間の制限
});