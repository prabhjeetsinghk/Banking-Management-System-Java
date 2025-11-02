document.addEventListener("DOMContentLoaded", function () {

    const token = localStorage.getItem("token");

    // Dashboard page
    if (document.getElementById("balance")) {
        fetch("/api/accounts/myaccounts", {
            headers: { "Authorization": "Bearer " + token }
        })
            .then(res => res.json())
            .then(accounts => {
                if (accounts.length > 0) {
                    document.getElementById("balance").textContent = accounts[0].balance;
                }
            });

        fetch("/api/accounts/transactions/recent", {
            headers: { "Authorization": "Bearer " + token }
        })
            .then(res => res.json())
            .then(transactions => {
                const tbody = document.getElementById("transactionsTable");
                transactions.forEach(txn => {
                    const row = `<tr>
                    <td>${txn.id}</td>
                    <td>${txn.type}</td>
                    <td>${txn.amount}</td>
                    <td>${txn.transactionDate}</td>
                    <td>${txn.fromAccount ? txn.fromAccount.accountNumber : "-"}</td>
                    <td>${txn.toAccount ? txn.toAccount.accountNumber : "-"}</td>
                </tr>`;
                    tbody.innerHTML += row;
                });
            });
    }

    // Transfer page
    const transferForm = document.getElementById("transferForm");
    if (transferForm) {
        transferForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const email = document.getElementById("email").value;
            const amount = document.getElementById("amount").value;

            fetch("/api/accounts/transfer", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({ email, amount })
            })
                .then(res => res.json())
                .then(data => alert("Transfer Successful"))
                .catch(err => alert("Error: " + err));
        });
    }

});
