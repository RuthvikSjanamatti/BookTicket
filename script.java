// Ensure all DOM content is loaded before running the script
document.addEventListener('DOMContentLoaded', () => {

    // --- DOM Elements ---
    const movieListingsContainer = document.getElementById('movie-listings');
    const bookingModal = document.getElementById('booking-modal');
    const modalContentContainer = document.getElementById('modal-content-container');
    const closeModalBtn = document.getElementById('close-modal');
    const modalTitle = document.getElementById('modal-title');
    const modalMoviePoster = document.getElementById('modal-movie-poster');
    const modalMovieGenre = document.getElementById('modal-movie-genre');
    const modalMovieDuration = document.getElementById('modal-movie-duration');
    const modalMovieRating = document.getElementById('modal-movie-rating');
    const showtimeSelect = document.getElementById('showtime-select');
    const seatSelectionArea = document.getElementById('seat-selection-area');
    const seatsGrid = document.getElementById('seats-grid');
    const ticketDetailsArea = document.getElementById('ticket-details-area');
    const numTicketsInput = document.getElementById('num-tickets');
    const totalPriceSpan = document.getElementById('total-price');
    const bookTicketsBtn = document.getElementById('book-tickets-btn');
    const cancelTicketsBtn = document.getElementById('cancel-tickets-btn');
    const confirmationMessage = document.getElementById('confirmation-message');
    const errorMessage = document.getElementById('error-message');
    const errorText = document.getElementById('error-text');

    // --- Sample Data (In a real app, this would come from a backend API) ---
    let movies = [
        {
            id: 'movie1',
            title: 'Dune: Part Two',
            genre: 'Sci-Fi, Action',
            duration: '166 mins',
            rating: '8.8/10',
            poster: 'https://placehold.co/200x300/4F46E5/FFFFFF?text=DUNE+2', // Example placeholder
            showtimes: [
                { time: '10:00 AM', price: 250, seats: generateSeats(80) },
                { time: '02:00 PM', price: 250, seats: generateSeats(80) },
                { time: '06:00 PM', price: 300, seats: generateSeats(80) }
            ]
        },
        {
            id: 'movie2',
            title: 'Godzilla x Kong: The New Empire',
            genre: 'Action, Sci-Fi',
            duration: '115 mins',
            rating: '7.0/10',
            poster: 'https://placehold.co/200x300/F97316/FFFFFF?text=G+x+K', // Example placeholder
            showtimes: [
                { time: '11:00 AM', price: 220, seats: generateSeats(70) },
                { time: '03:30 PM', price: 250, seats: generateSeats(70) },
                { time: '08:00 PM', price: 280, seats: generateSeats(70) }
            ]
        },
        {
            id: 'movie3',
            title: 'Kung Fu Panda 4',
            genre: 'Animation, Family',
            duration: '94 mins',
            rating: '7.5/10',
            poster: 'https://placehold.co/200x300/22C55E/FFFFFF?text=KFP+4', // Example placeholder
            showtimes: [
                { time: '09:30 AM', price: 200, seats: generateSeats(60) },
                { time: '01:00 PM', price: 220, seats: generateSeats(60) },
                { time: '05:00 PM', price: 250, seats: generateSeats(60) }
            ]
        },
        {
            id: 'movie4',
            title: 'Ghostbusters: Frozen Empire',
            genre: 'Comedy, Fantasy',
            duration: '115 mins',
            rating: '6.5/10',
            poster: 'https://placehold.co/200x300/06B6D4/FFFFFF?text=Ghostbusters', // Example placeholder
            showtimes: [
                { time: '10:30 AM', price: 230, seats: generateSeats(75) },
                { time: '02:30 PM', price: 260, seats: generateSeats(75) },
                { time: '07:30 PM', price: 290, seats: generateSeats(75) }
            ]
        }
    ];

    // --- State Variables ---
    let currentMovie = null;
    let currentShowtime = null;
    let selectedSeats = [];
    let currentBookingId = 0; // Simple counter for booking IDs
    let lastBookedSeatsForCancellation = []; // NEW: To store seats of the last booking for cancellation

    // --- Functions ---

    /**
     * Generates a flat array of seat objects for a given number of seats.
     * Each seat has a unique ID, a status ('available', 'booked'), and a row/col for display.
     * Some seats are randomly marked as 'booked'.
     * @param {number} count The total number of seats to generate.
     * @returns {Array<Object>} An array of seat objects.
     */
    function generateSeats(count) {
        const seats = [];
        for (let i = 0; i < count; i++) {
            const row = String.fromCharCode(65 + Math.floor(i / 10)); // A, B, C...
            const col = (i % 10) + 1;
            seats.push({
                id: seat-${row}${col},
                row: row,
                col: col,
                status: Math.random() < 0.2 ? 'booked' : 'available' // 20% seats initially booked
            });
        }
        return seats;
    }

    /**
     * Renders movie cards to the DOM.
     */
    function renderMovies() {
        movieListingsContainer.innerHTML = ''; // Clear existing listings
        movies.forEach(movie => {
            const movieCard = document.createElement('div');
            movieCard.className = 'movie-card bg-gray-800 rounded-xl shadow-xl overflow-hidden cursor-pointer transform hover:scale-105 transition-all duration-300';
            movieCard.innerHTML = `
                <img src="${movie.poster}" alt="${movie.title} Poster" class="w-full h-72 object-cover object-center rounded-t-xl">
                <div class="p-6">
                    <h3 class="text-2xl font-bold text-white mb-2">${movie.title}</h3>
                    <p class="text-gray-400 text-sm mb-1"><span class="font-semibold">Genre:</span> ${movie.genre}</p>
                    <p class="text-gray-400 text-sm mb-1"><span class="font-semibold">Duration:</span> ${movie.duration}</p>
                    <p class="text-gray-400 text-sm mb-4"><span class="font-semibold">Rating:</span> ${movie.rating}</p>
                    <button data-movie-id="${movie.id}" class="book-movie-btn w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-6 rounded-lg shadow-md transition duration-300 transform hover:scale-105 focus:outline-none focus:ring-4 focus:ring-red-500 focus:ring-opacity-50">
                        Book Now
                    </button>
                </div>
            `;
            movieListingsContainer.appendChild(movieCard);
        });

        // Add event listeners to "Book Now" buttons
        document.querySelectorAll('.book-movie-btn').forEach(button => {
            button.addEventListener('click', (event) => {
                const movieId = event.target.dataset.movieId;
                openBookingModal(movieId);
            });
        });
    }

    /**
     * Opens the booking modal and populates it with movie details and showtimes.
     * @param {string} movieId The ID of the movie to book.
     */
    function openBookingModal(movieId) {
        currentMovie = movies.find(movie => movie.id === movieId);
        if (!currentMovie) {
            console.error('Movie not found:', movieId);
            return;
        }

        // Reset modal state
        selectedSeats = [];
        seatSelectionArea.classList.add('hidden');
        ticketDetailsArea.classList.add('hidden');
        confirmationMessage.classList.add('hidden');
        errorMessage.classList.add('hidden');
        cancelTicketsBtn.classList.add('hidden');
        bookTicketsBtn.classList.remove('hidden');
        numTicketsInput.value = 1;
        updateTotalPrice();


        // Populate modal details
        modalTitle.textContent = Book Tickets for ${currentMovie.title};
        modalMoviePoster.src = currentMovie.poster;
        modalMovieGenre.textContent = currentMovie.genre;
        modalMovieDuration.textContent = currentMovie.duration;
        modalMovieRating.textContent = currentMovie.rating;

        // Populate showtimes dropdown
        showtimeSelect.innerHTML = '<option value="" disabled selected>Select a showtime</option>';
        currentMovie.showtimes.forEach((showtime, index) => {
            const option = document.createElement('option');
            option.value = index; // Use index to reference the showtime object
            option.textContent = ${showtime.time} (₹${showtime.price});
            showtimeSelect.appendChild(option);
        });

        // Show modal with animation
        bookingModal.classList.remove('hidden');
        requestAnimationFrame(() => {
            modalContentContainer.classList.remove('opacity-0', 'scale-95');
            modalContentContainer.classList.add('opacity-100', 'scale-100');
        });
    }

    /**
     * Closes the booking modal with animation.
     */
    function closeBookingModal() {
        modalContentContainer.classList.remove('opacity-100', 'scale-100');
        modalContentContainer.classList.add('opacity-0', 'scale-95');
        modalContentContainer.addEventListener('transitionend', () => {
            bookingModal.classList.add('hidden');
        }, { once: true }); // Remove listener after one transition
    }

    /**
     * Renders seats for the selected showtime.
     */
    function renderSeats() {
        seatsGrid.innerHTML = ''; // Clear previous seats
        if (!currentShowtime) return;

        // Set grid columns dynamically based on max 10 columns
        const numCols = Math.min(10, currentShowtime.seats.length);
        seatsGrid.style.gridTemplateColumns = repeat(${numCols}, minmax(0, 1fr));

        currentShowtime.seats.forEach(seat => {
            const seatElement = document.createElement('div');
            seatElement.className = `seat w-8 h-8 rounded-md flex items-center justify-center font-semibold text-xs transition duration-200 ease-in-out
                ${seat.status === 'available' ? 'bg-green-500 hover:bg-green-600 text-white cursor-pointer' :
                   seat.status === 'booked' ? 'bg-red-600 text-white cursor-not-allowed' :
                   'bg-blue-500 text-white border-2 border-blue-700'}`; // 'selected' status

            seatElement.textContent = ${seat.row}${seat.col};
            seatElement.dataset.seatId = seat.id;
            seatElement.dataset.status = seat.status;

            if (seat.status === 'available') {
                seatElement.addEventListener('click', toggleSeatSelection);
            }
            seatsGrid.appendChild(seatElement);
        });
    }

    /**
     * Toggles the selection of a seat.
     * @param {Event} event The click event.
     */
    function toggleSeatSelection(event) {
        const seatElement = event.target;
        const seatId = seatElement.dataset.seatId;
        const seatStatus = seatElement.dataset.status;

        if (seatStatus === 'booked') {
            displayError('This seat is already booked!');
            return;
        }

        const isSelected = selectedSeats.includes(seatId);

        if (isSelected) {
            // Deselect seat
            selectedSeats = selectedSeats.filter(id => id !== seatId);
            seatElement.classList.remove('bg-blue-500', 'border-2', 'border-blue-700');
            seatElement.classList.add('bg-green-500', 'hover:bg-green-600');
        } else {
            // Select seat
            if (selectedSeats.length >= parseInt(numTicketsInput.value)) {
                displayError(You can only select ${numTicketsInput.value} seat(s).);
                return;
            }
            selectedSeats.push(seatId);
            seatElement.classList.remove('bg-green-500', 'hover:bg-green-600');
            seatElement.classList.add('bg-blue-500', 'border-2', 'border-blue-700');
        }
        updateTotalPrice();
    }

    /**
     * Updates the total price based on selected tickets and showtime price.
     */
    function updateTotalPrice() {
        if (currentShowtime) {
            const numTickets = selectedSeats.length; // Use selected seats count for price calculation
            const totalPrice = numTickets * currentShowtime.price;
            totalPriceSpan.textContent = ₹${totalPrice.toFixed(2)};
        } else {
            totalPriceSpan.textContent = '₹0.00';
        }
    }

    /**
     * Displays a confirmation message.
     * @param {string} message The message to display.
     */
    function displayConfirmation(message) {
        confirmationMessage.querySelector('p:last-child').textContent = message;
        confirmationMessage.classList.remove('hidden');
        errorMessage.classList.add('hidden'); // Hide error if confirmation is shown
        // Optional: Hide after a few seconds
        setTimeout(() => {
            confirmationMessage.classList.add('hidden');
        }, 5000);
    }

    /**
     * Displays an error message.
     * @param {string} message The message to display.
     */
    function displayError(message) {
        errorText.textContent = message;
        errorMessage.classList.remove('hidden');
        confirmationMessage.classList.add('hidden'); // Hide confirmation if error is shown
        // Optional: Hide after a few seconds
        setTimeout(() => {
            errorMessage.classList.add('hidden');
        }, 5000);
    }

    /**
     * Handles the movie booking process.
     */
    function bookTickets() {
        if (!currentMovie || !currentShowtime) {
            displayError('Please select a movie and showtime.');
            return;
        }

        if (selectedSeats.length === 0) {
            displayError('Please select at least one seat.');
            return;
        }

        // Simulate booking - mark selected seats as booked
        const bookedSeatsDetails = [];
        selectedSeats.forEach(seatId => {
            const seatIndex = currentShowtime.seats.findIndex(seat => seat.id === seatId);
            if (seatIndex !== -1) {
                currentShowtime.seats[seatIndex].status = 'booked';
                bookedSeatsDetails.push(currentShowtime.seats[seatIndex].id); // Store seat IDs for cancellation
            }
        });

        // Store these booked seats for potential cancellation
        lastBookedSeatsForCancellation = [...bookedSeatsDetails]; // Store a copy

        // Increment booking ID (simple simulation)
        currentBookingId++;
        const bookingRef = {
            id: booking-${currentBookingId},
            movieId: currentMovie.id,
            movieTitle: currentMovie.title,
            showtime: currentShowtime.time,
            seats: bookedSeatsDetails,
            totalPrice: parseFloat(totalPriceSpan.textContent.replace('₹', ''))
        };

        // In a real app, send bookingRef to backend
        console.log('Booking successful:', bookingRef);
        displayConfirmation(Your booking for ${currentMovie.title} at ${currentShowtime.time} for seats ${bookedSeatsDetails.join(', ')} is confirmed! Booking ID: ${bookingRef.id}.);

        // After successful booking, hide book button and show cancel button
        bookTicketsBtn.classList.add('hidden');
        cancelTicketsBtn.classList.remove('hidden');
        cancelTicketsBtn.dataset.bookingId = bookingRef.id; // Store booking ID for cancellation

        // Re-render seats to reflect booked status
        renderSeats();

        // Optionally, reset selected seats for next booking attempt
        selectedSeats = [];
        updateTotalPrice();
    }

    /**
     * Handles the ticket cancellation process.
     * In a real app, this would require authentication and validating the booking ID.
     */
    function cancelTickets() {
        if (!lastBookedSeatsForCancellation || lastBookedSeatsForCancellation.length === 0) {
            displayError('No active booking details found for cancellation.');
            return;
        }

        if (!currentMovie || !currentShowtime) {
            displayError('Movie or showtime context lost for cancellation.');
            return;
        }

        let cancelledCount = 0;
        lastBookedSeatsForCancellation.forEach(seatIdToCancel => {
            const seatIndex = currentShowtime.seats.findIndex(seat => seat.id === seatIdToCancel);
            if (seatIndex !== -1) {
                // Only change status if it was actually booked
                if (currentShowtime.seats[seatIndex].status === 'booked') {
                    currentShowtime.seats[seatIndex].status = 'available';
                    cancelledCount++;
                }
            }
        });

        if (cancelledCount > 0) {
            displayConfirmation(Successfully cancelled ${cancelledCount} ticket(s) for ${currentMovie.title} at ${currentShowtime.time}.);
        } else {
            displayError('No previously booked seats found to cancel for this showtime.');
        }


        // Reset UI state
        lastBookedSeatsForCancellation = []; // Clear the reference for future bookings
        selectedSeats = []; // Ensure selectedSeats is also cleared
        updateTotalPrice(); // Update total price (will be 0)
        renderSeats(); // Re-render seats to reflect changes
        bookTicketsBtn.classList.remove('hidden');
        cancelTicketsBtn.classList.add('hidden');
        delete cancelTicketsBtn.dataset.bookingId; // Clear booking ID
    }


    // --- Event Listeners ---

    // Close modal when close button is clicked
    closeModalBtn.addEventListener('click', closeBookingModal);

    // Close modal when clicking outside the modal content
    bookingModal.addEventListener('click', (event) => {
        if (!modalContentContainer.contains(event.target) && event.target !== closeModalBtn) {
            closeBookingModal();
        }
    });

    // Handle showtime selection change
    showtimeSelect.addEventListener('change', (event) => {
        const selectedIndex = parseInt(event.target.value);
        currentShowtime = currentMovie.showtimes[selectedIndex];
        selectedSeats = []; // Reset selected seats when showtime changes
        renderSeats();
        updateTotalPrice();
        seatSelectionArea.classList.remove('hidden');
        ticketDetailsArea.classList.remove('hidden');
        errorMessage.classList.add('hidden'); // Clear any previous errors
        confirmationMessage.classList.add('hidden'); // Clear any previous confirmations
    });

    // Update total price when number of tickets changes (for initial selection, then seats take precedence)
    numTicketsInput.addEventListener('input', () => {
        const requestedTickets = parseInt(numTicketsInput.value);
        if (requestedTickets < 1) {
            numTicketsInput.value = 1;
        }
        // If selected seats exceed new number of tickets, deselect some
        if (selectedSeats.length > requestedTickets) {
            selectedSeats = selectedSeats.slice(0, requestedTickets);
            renderSeats(); // Re-render to update visual selection
        }
        updateTotalPrice();
    });

    // Book Tickets button click
    bookTicketsBtn.addEventListener('click', bookTickets);

    // Cancel Tickets button click
    cancelTicketsBtn.addEventListener('click', cancelTickets);


    // --- Initial Render ---
    renderMovies();

    // --- Gemini API Placeholder (Example for future use) ---
    async function generateTextWithGemini(prompt) {
        let chatHistory = [];
        chatHistory.push({ role: "user", parts: [{ text: prompt }] });
        const payload = { contents: chatHistory };
        // If you want to use models other than gemini-2.0-flash, provide an API key here.
        // Otherwise, leave this as-is. Canvas will automatically provide it in runtime.
        const apiKey = "";
        const apiUrl = https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey};

        try {
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const result = await response.json();
            if (result.candidates && result.candidates.length > 0 &&
                result.candidates[0].content && result.candidates[0].content.parts &&
                result.candidates[0].content.parts.length > 0) {
                const text = result.candidates[0].content.parts[0].text;
                console.log("Gemini API Response:", text);
                return text;
            } else {
                console.error("Gemini API: Unexpected response structure or no content.");
                return "Failed to generate text.";
            }
        } catch (error) {
            console.error("Error calling Gemini API:", error);
            return "Error calling text generation service.";
        }
    }

    // Example of calling the Gemini API (uncomment to test)
    // generateTextWithGemini("Write a short, exciting summary for a new action movie.");
});
