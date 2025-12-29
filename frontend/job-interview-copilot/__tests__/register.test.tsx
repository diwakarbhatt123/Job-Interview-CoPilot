import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import RegisterPage from "@/pages/register";

const push = jest.fn();
const replace = jest.fn();

jest.mock("next/router", () => ({
  useRouter: () => ({
    push,
    replace,
    query: {},
  }),
}));

beforeEach(() => {
  push.mockReset();
  replace.mockReset();
  global.fetch = jest.fn();
});

it("shows password mismatch error", async () => {
  render(<RegisterPage />);

  fireEvent.change(screen.getByLabelText(/^password$/i), {
    target: {value: "secret"},
  });
  fireEvent.change(screen.getByLabelText(/confirm password/i), {
    target: {value: "different"},
  });

  expect(await screen.findByText(/passwords do not match/i)).toBeInTheDocument();
});

it("shows error on duplicate email", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 409,
  });

  render(<RegisterPage />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/^password$/i), {
    target: {value: "secret"},
  });
  fireEvent.change(screen.getByLabelText(/confirm password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /register/i}));

  expect(
    await screen.findByText(/email already exists/i)
  ).toBeInTheDocument();
});

it("redirects on success", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: true,
    status: 200,
  });

  render(<RegisterPage />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/^password$/i), {
    target: {value: "secret"},
  });
  fireEvent.change(screen.getByLabelText(/confirm password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /register/i}));

  await waitFor(() => expect(replace).toHaveBeenCalledWith("/login"));
});

it("shows error on invalid input", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 400,
  });

  render(<RegisterPage />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/^password$/i), {
    target: {value: "secret"},
  });
  fireEvent.change(screen.getByLabelText(/confirm password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /register/i}));

  expect(
    await screen.findByText(/invalid input/i)
  ).toBeInTheDocument();
});

it("shows network error on fetch failure", async () => {
  (global.fetch as jest.Mock).mockRejectedValue(new Error("Network error"));

  render(<RegisterPage />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/^password$/i), {
    target: {value: "secret"},
  });
  fireEvent.change(screen.getByLabelText(/confirm password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /register/i}));

  expect(
    await screen.findByText(/network error/i)
  ).toBeInTheDocument();
});
