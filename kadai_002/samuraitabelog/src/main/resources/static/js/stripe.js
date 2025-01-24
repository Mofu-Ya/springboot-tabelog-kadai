const stripe = Stripe('pk_test_51QSHY1GlJNPcJx2JxvexiiQGgZkEQh4RPVHbbqtQSHJxXmV58zeF2JwSZs5QFiFXHogYt4fqEJbUqYcAbP74g2sn00BEWtUxDz');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId: sessionId
	})
});